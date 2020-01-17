package org.overworld.metre.score;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.overworld.metre.ApplicationException;
import org.overworld.metre.CommunicationException;

/**
 * Stores a list of ScoreCards representing multiple test runs performed in one
 * Metre invocation
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-04-13
 */
public class ScoreTable extends TreeSet<ScoreCard> {

    public static final int BREAK_LINE_LEN = 90;
    public static Path outdir;

    private static String resultTemplate;

    private static final long serialVersionUID = 1L;

    /**
     * The template for the resulting GPlot graph, found in the classpath by
     * name
     */
    public static final String TEMPLATE_NAME = "ResultTemplate.gplot";

    /**
     * Word wrap a string so that it breaks on the space immediately after
     * BREAK_LINE_LEN
     *
     * @param input
     *            a string that may need line wrapping
     * @return the string word wrapped by the insertion of newlines
     */
    public static String wordWrap(final String input) {

        final ArrayList<String> parts = new ArrayList<String>(Arrays.asList(
            input.split("\\s+")));

        int lengthSinceBreak = 0;

        for (int part = 0; part < parts.size() - 1; part++) {

            lengthSinceBreak += parts.get(part).length();

            if (lengthSinceBreak >= BREAK_LINE_LEN) {

                /* add both a \n for glfot and a newline for emphasis */
                parts.add(part + 1, "\\n\\" + System.lineSeparator());

                /* since we have added a new part, we can skip one */
                part++;

                /* we added a break, so restart counting length from break */
                lengthSinceBreak = 0;
            }
        }

        return String.join(" ", parts);
    }

    /**
     * Produce a complete GPlot file from a ScoreCard instance
     */
    public static void writeScore(final ScoreCard sc, final int position)
        throws CommunicationException {

        /* @formatter:off */

        final String result = String.format(resultTemplate,
            sc.getRunNumber(),
            sc.getRuntime(),
            sc.getCpuTime(),
            sc.getScore(),
            wordWrap(sc.getJvmArgs()),
            sc.getMinorCollectionName(),
            sc.getMinorCollectionCount(),
            sc.getMinorCollectionTime(),
            sc.getMajorCollectionName(),
            sc.getMajorCollectionCount(),
            sc.getMajorCollectionTime(),
            sc.getHeapUsageSeries(),
            sc.getNonHeapUsageSeries(),
            sc.getCpuSeries()
            );

        /* @formatter:on */

        final String filename = String.format("%04d.gplot", position);

        try (BufferedWriter writer = Files.newBufferedWriter(outdir
            .resolve(filename), Charset.forName("US-ASCII"))) {
            writer.write(result, 0, result.length());
        } catch (final IOException e) {

            throw new CommunicationException("Error writing GPlot file "
                + filename, e);
        }
    }

    /**
     * Construct a new ScoreTable and initialise the output directory and GPlot
     * template
     *
     * @throws ApplicationException
     *             on error writing the output directory or files
     */
    public ScoreTable() throws ApplicationException {

        synchronized (ScoreTable.class) {

            outdir = FileSystems.getDefault().getPath("scorecards");

            resultTemplate = new BufferedReader(
                new InputStreamReader(this.getClass().getClassLoader()
                    .getResourceAsStream(TEMPLATE_NAME))).lines().collect(
                        Collectors.joining(System.lineSeparator()));
        }

        if (!Files.exists(outdir)) {
            try {

                Files.createDirectory(outdir);
            } catch (final IOException e) {

                throw new ApplicationException(
                    "Unable to create output directory " + outdir, e);
            }
        }
    }

    /**
     * Write GFlot scorecards for the topmost n entries, where order is defined
     * in the Set
     *
     * @param number
     *            the number of topmost entries
     * @throws IOException
     *             on error writing the output file
     */
    public void dumpTop(int number) throws CommunicationException {

        if (number < 1)
            throw new IllegalArgumentException(
                "Number of top entries to dump cannot be negative: " + number);

        if (number > this.size()) number = this.size();

        final Iterator<ScoreCard> iter = this.descendingIterator();
        for (int i = 1; i <= number; ++i) {

            ScoreTable.writeScore((ScoreCard) iter.next(), i);
        }
    }
}
