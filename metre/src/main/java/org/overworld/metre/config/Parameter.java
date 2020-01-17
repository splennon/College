package org.overworld.metre.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a parameter in the application configuration file.
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 *
 */
public abstract class Parameter {

    protected String description;

    /**
     * @param description
     *            the parameter description
     */
    protected Parameter(final String description) {

        this.description = description;
    }

    /**
     * Advances the value once in its search pattern
     */
    public abstract boolean advance();

    /**
     * @return every value of this parameter appended to every value in base
     */
    public List<String> expand(final List<String> base) {

        final List<String> results = new ArrayList<>();

        do {

            final String append = this.print();

            for (final String s : base) {
                results.add(s + " " + append);
            }
        } while (this.advance());

        return results;
    }

    /**
     * @return the description of this parameter
     */
    public String getDescription() {

        return this.description;
    }

    /**
     * @return true if there are more values in the search pattern, false
     *         otherwise
     */
    public abstract boolean hasMore();

    /**
     * @return the parameter and value correctly formatted for inclusion on the
     *         command line
     */
    public abstract String print();

    /**
     * restart the search pattern at the starting value
     */
    public abstract void reset();
}
