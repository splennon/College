package org.overworld.example.webservice.engine;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;

/**
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016
 */

public class MD5Sum implements Function<String, String> {

    /**
     * Converts a block of text to its MD5 digest, outputting the result with
     * hex encoding.
     *
     * @param input
     *            the text to digest
     * @return the MD5 digest encoded as hex
     */
    private static String md5sum(final String input) {

        try {

            final MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] digest = md.digest(input.getBytes("UTF-8"));
            final BigInteger bint = new BigInteger(1, digest);
            return String.format("%0" + (digest.length << 1) + "x", bint);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {

            /* UTF-8 or MD5 really cannot be unsupported */
            throw new RuntimeException(
                "Encoding UTF-8 or Algorythm MD5 not supported");
        }
    }

    @Override
    public String apply(final String input) {

        return md5sum(input);
    }
}