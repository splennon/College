package org.overworld.example.webservice.engine;

import static org.junit.Assert.assertEquals;

import java.util.function.Function;

import org.junit.Test;

/**
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016
 */

public class BSDSumTest {

    @Test
    public void bsdSumTest() {

        final Function<String, String> bsd = new BSDSum();

        assertEquals("45436 1",
            bsd.apply("The quick brown fox jumps over the lazy dog.\n"));
    }
}
