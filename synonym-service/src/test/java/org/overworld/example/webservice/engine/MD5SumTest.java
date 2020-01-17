package org.overworld.example.webservice.engine;

import static org.junit.Assert.assertEquals;

import java.util.function.Function;

import org.junit.Test;

/**
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016
 */

public class MD5SumTest {

    @Test
    public void md5SumTest() {

        final Function<String, String> md5 = new MD5Sum();

        assertEquals("0d7006cd055e94cf614587e1d2ae0c8e",
            md5.apply("The quick brown fox jumps over the lazy dog.\n"));
    }
}