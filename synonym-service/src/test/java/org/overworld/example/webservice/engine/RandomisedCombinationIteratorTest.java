package org.overworld.example.webservice.engine;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

/**
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016
 */

public class RandomisedCombinationIteratorTest {

    @Test
    public void test() {

        final int[] lengths = new int[]{ 3,4,3,4,5,7,1,2 };
        final RandomisedCombinationIterator iter = new RandomisedCombinationIterator(lengths);

        /* let's use indirects that increment the structure from right to left
         * instead of the normal left to right */

        iter.setIndirects(new int[] { 7,6,5,4,3,2,1,0 });

        assertArrayEquals(new int[] { 0,0,0,0,0,0,0,1 }, iter.next());
        assertArrayEquals(new int[] { 0,0,0,0,0,1,0,0 }, iter.next());
        assertArrayEquals(new int[] { 0,0,0,0,0,1,0,1 }, iter.next());
        assertArrayEquals(new int[] { 0,0,0,0,0,2,0,0 }, iter.next());
        assertArrayEquals(new int[] { 0,0,0,0,0,2,0,1 }, iter.next());
        assertArrayEquals(new int[] { 0,0,0,0,0,3,0,0 }, iter.next());
        assertArrayEquals(new int[] { 0,0,0,0,0,3,0,1 }, iter.next());
        assertArrayEquals(new int[] { 0,0,0,0,0,4,0,0 }, iter.next());
        assertArrayEquals(new int[] { 0,0,0,0,0,4,0,1 }, iter.next());
        assertArrayEquals(new int[] { 0,0,0,0,0,5,0,0 }, iter.next());
        assertArrayEquals(new int[] { 0,0,0,0,0,5,0,1 }, iter.next());
        assertArrayEquals(new int[] { 0,0,0,0,0,6,0,0 }, iter.next());
        assertArrayEquals(new int[] { 0,0,0,0,0,6,0,1 }, iter.next());
        assertArrayEquals(new int[] { 0,0,0,0,1,0,0,0 }, iter.next());
    }
}
