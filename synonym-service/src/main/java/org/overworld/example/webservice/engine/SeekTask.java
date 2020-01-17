package org.overworld.example.webservice.engine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Attempts to manipulate a block of text by replacing non-stopwords with
 * synonyms until it matches the MD5 digest specified.
 * <p/>
 * Note that this will not match the trivial case where the text already has the
 * digest specified as it is then assumed that the caller wants a different
 * block of text.
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016
 */
public class SeekTask implements Callable<String> {

    /**
     * Stopwords as a set
     */
    private static SortedSet<String> stopwords;

    /**
     * The startText broken down into alternating words and whitespace
     */
    private String[] digestedText;

    /**
     * The digest function that turns the message into its digest
     */
    private final Function<String, String> digester;

    /**
     * The generator is a 2-dimensional structure containing all words
     * (Identified by their index in digestedText), against a list of all
     * possible synonyms for that word. Stopwords are not eligible for expansion
     * and so only the original word appears in the generator
     */
    private final List<List<String>> generator = new ArrayList<>();

    /**
     * For convenience, the lengths of each List inside the generator, in order
     */
    private int[] generatorLengths;

    /**
     * The system time in ms of the last exception
     */
    private long lastException = 0;

    /**
     * The duration in ms within which two upstream 500 errors aborts the task
     */
    @Value("${upstream.maxExceptionWindow}")
    private int maxExceptionWindow;

    /**
     * An integer representing the current phase of processing in the task
     */
    private final AtomicInteger progress;

    /**
     * The starting text to mutate
     */
    private final String startText;

    /**
     * A list of stopwords to exclude from synonym expansion
     */
    @Value("${engine.stopwords}")
    private String stopwordsCSV;

    /**
     * The target checksum or digest to achieve
     */
    private final String targetDigest;

    /**
     * The URL of the upstream Synonym Service
     */
    @Value("${upstream.synonym.url}")
    private String url;

    /**
     * A function that maps counted words to their index in digestedText
     */
    private Function<Integer, Integer> wordAt;

    /**
     * Find a variation of the startText by substituting synonyms for words
     * therein such that the digest of the new text matches the targetDigest as
     * provided by the function digester
     *
     * @param startText
     *            the starting message to alter with synonyms
     * @param targetDigest
     *            the resulting message digest to seek
     * @param digester
     *            the function that digests the message into a string digest
     * @param progress
     *            an integer that indicates where the algorythm is currently in
     *            its computation
     */
    public SeekTask(final String startText, final String targetDigest,
        final Function<String, String> digester, final AtomicInteger progress) {

        this.digester = digester;
        this.startText = startText;
        this.progress = progress;
        this.progress.set(0);
        this.targetDigest = targetDigest.toLowerCase();
    }

    /**
     * @return a new text that matches the checksum given, or null
     */
    @Override
    public String call() {

        stopwords = new TreeSet<String>(Arrays.asList(this.stopwordsCSV.split(",")));

        this.progress.set(1);

        this.prepare();

        this.progress.set(2);

        this.makeGenerator();

        this.progress.set(3);

        this.generatorLengths = this.generator.stream().mapToInt(los -> los.size())
            .toArray();

        this.progress.set(4);

        final RandomisedCombinationIterator iter = new RandomisedCombinationIterator(
            this.generatorLengths);

        this.progress.set(5);

        String attempt;

        try {

            do {

                if (Thread.interrupted())
                    return null;

                attempt = this.generateText(iter.next());
            } while (!this.digester.apply(attempt).equals(this.targetDigest));

            return attempt;

        } catch (final IndexOutOfBoundsException e) {

            /* all permutations exhausted without match */
            return null;
        } finally {

            this.progress.set(6);
        }
    }

    /**
     * Uses the instance's generator and digestedText to output a possible
     * textual permutation corresponding to the state configuration given
     *
     * @param state
     *            the state of the permutations inside the generator to apply to
     *            generating the output test
     * @return a new body of text generated by applying a different state
     *         against this instance's generator and digestedText
     */
    private String generateText(final int[] state) {

        final String[] newText = this.digestedText.clone();

        for (int i = 0; i < this.generator.size(); i++) {
            newText[this.wordAt.apply(i)] = this.generator.get(i).get(state[i]);
        }

        return String.join("", newText);
    }

    /**
     * @param the
     *            word to expand into synonyms
     * @return word a list of synonyms for the word given
     */
    private List<String> getSynonyms(final String word) {

        final boolean uppercaseFirst = Character.isUpperCase(word.charAt(0));

        final List<String> result = new ArrayList<>();

        result.add(word);

        final String expandedURL = this.url.replace("{}", word.toLowerCase());

        final JsonNode fromAPI = this.wget(expandedURL);

        if (fromAPI != null) {

            JsonNode response = null;
            while (response == null) {
                response = this.wget(expandedURL);
            }

            final List<JsonNode> lojn = response.findValues("syn");

            for (final JsonNode jn : lojn) {
                for (final JsonNode inner : jn) {

                    String synonym = inner.textValue();

                    if (uppercaseFirst) {
                        synonym = synonym.substring(0, 1).toUpperCase()
                            + synonym.substring(1);
                    }

                    result.add(synonym);
                }
            }
        }

        return result;
    }

    /**
     * A private utility method to construct the 2 dimensional generator by
     * expanding all non-whtespace words in digestedText
     */
    private void makeGenerator() {

        int i = 0;
        while (this.digestedText.length > this.wordAt.apply(i)) {

            final String thisWord = this.digestedText[this.wordAt.apply(i)];

            if (stopwords.contains(thisWord.toLowerCase())) {

                this.generator.add(Arrays.asList(new String[] { thisWord }));
            } else {

                /*
                 * At this point we have an eligible word, not stopword, not
                 * whitespace
                 */

                this.generator.add(this.getSynonyms(thisWord));
            }

            i++;

            assert this.generator.size() == i;

            if (Thread.interrupted())
                return;
        }
    }

    /**
     * A utility function to split the text into words and surrounding non-words
     * and prepare a function to index it easily.
     *
     * @throws IllegalArgumentException
     *             if splitting the text results in empty result
     */
    private void prepare() throws IllegalArgumentException {

        /*
         * When a string is split on a word boundary every second result is a
         * word, and every other second result is a non-word sequence
         */

        this.digestedText = this.startText.split("\\b");

        if (this.digestedText.length < 1)
            throw new IllegalArgumentException("Text is empty after prepare");

        /*
         * However the first may be either word or nonword, so construct a
         * partially resolved function to map that
         */

        this.wordAt = (index) -> {
            return (index * 2) + (this.digestedText[0].matches("^\\W") ? 1 : 0);
        };
    }

    /**
     * Reads JSON from a given URL
     *
     * @param getURL
     *            the URL to retrieve
     * @return the JsonNode retrieved from the URL given
     */
    private JsonNode wget(final String getURL) {

        final ObjectMapper om = new ObjectMapper();

        try {

            return om.readTree(new BufferedReader(
                new InputStreamReader(new URL(getURL).openStream())));
        } catch (final FileNotFoundException e) {

            return null;
        } catch (final IOException e) {

            if (this.lastException > System.currentTimeMillis()
                - this.maxExceptionWindow) {

                throw new RuntimeException(
                    "IO Exceptions from upstream service are too frequent");
            } else {

                this.lastException = System.currentTimeMillis();

                return null;
            }
        }
    }
}
