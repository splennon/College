package org.overworld.example.webservice.engine;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a collection of digits, each of which has a different base, and
 * increments through the resulting sequence. The digits are incremented in a
 * random order that does not change for the life of the iterator.
 * <p/>
 * You can visualise this class like an odometer of a car except that each digit
 * can have any base independent of the base of other digits, not just 10, and
 * the dials are in random order, so the fastest moving dial is not necessarily
 * at the start or end of the array. However, it iterates through all arrays of
 * integers that can be so represented without duplication.
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016
 */
public class RandomisedCombinationIterator {

    /**
     * The indirection array that confuses the order in which fields are
     * incremented
     */
    private int[] indirects;

    /**
     * The length of each list in the generator in order
     */
    private final int[] lenghts;

    /**
     * The current state
     */
    private final int[] state;

    /**
     * Create a new instance with the specified lengths and a randomised order
     * for incrementing fields. The starting position of the iterator is 0 for
     * every field.
     *
     * @param lengths
     *            the lengths of the ranges fields to be incremented
     */
    public RandomisedCombinationIterator(final int[] lengths) {

        this.lenghts = lengths;
        this.state = new int[this.lenghts.length];
        final List<Integer> indirectsList = IntStream.range(0, this.lenghts.length).boxed()
            .collect(Collectors.toList());
        Collections.shuffle(indirectsList);
        this.indirects = indirectsList.stream().mapToInt(i -> i.intValue())
            .toArray();
    }

    /**
     * The recursive case for incrementing the structure
     *
     * @param index
     *            the field to increment
     * @throws IndexOutOfBoundsException
     *             if there are no more sequences
     */
    private void inc(final int index) throws IndexOutOfBoundsException {

        final int indirectIndex = this.indirects[index];

        if (this.state[indirectIndex] < this.lenghts[indirectIndex] - 1) {

            /* There is room to increment the value at this index */
            this.state[indirectIndex]++;
        } else {

            /*
             * There is no room to increment the value at this index, so it
             * rolls around to 0 and the next one is incremented
             */
            this.state[indirectIndex] = 0;
            this.inc(index + 1);
        }
    }

    /**
     * Perform a recursive increment on the state and then return it
     *
     * @return the state, incremented by one
     * @throws IndexOutOfBoundsException
     *             if all states have been visited such that there is no next
     *             state
     */
    public int[] next() throws IndexOutOfBoundsException {

        this.inc(0);
        return this.state;
    }

    /**
     * Sets the indirects for unit testing only
     *
     * @param indirects
     *            the new indirects to use
     */
    void setIndirects(final int[] indirects) {

        this.indirects = indirects;
    }
}
