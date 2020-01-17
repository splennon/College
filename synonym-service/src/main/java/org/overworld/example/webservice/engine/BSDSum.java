package org.overworld.example.webservice.engine;

import java.io.UnsupportedEncodingException;
import java.util.function.Function;

/**
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016
 */

public class BSDSum implements Function<String, String> {

    /**
     * Converts a block of text to its BSD 16-bit checksum, formatted in
     * traditional BSD style as checksum and size
     *
     * @param input
     *            the input to be digested
     * @return the resulting BSD 16-bit checksum
     */
    private static String bsdSum(final String input) {

        int sum = 0;

        /*
         * The algorythm that follows is based on the example here:
         * https://docs.oracle.com/javase/8/docs/technotes/guides/io/example/Sum
         * .java
         */
        try {

            for (final byte b : input.getBytes("UTF-8")) {

                if ((sum & 1) != 0) {

                    sum = (sum >> 1) + 0x8000;
                } else {

                    sum >>= 1;
                }

                sum += b & 0xff;
                sum &= 0xffff;
            }
        } catch (final UnsupportedEncodingException e) {

            /* UTF-8 really cannot be unsupported */
            throw new RuntimeException("Encoding UTF-8 not supported");
        }

        final int kb = (input.length() + 1023) / 1024;
        return Integer.toString(sum) + " " + kb;
    }

    @Override
    public String apply(final String input) {

        return bsdSum(input);
    }
}