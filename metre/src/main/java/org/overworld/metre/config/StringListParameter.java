package org.overworld.metre.config;

/**
 * Represents a dynamic parameter that can take on a value from a list of
 * strings.
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class StringListParameter extends Parameter {

    private int index;
    private final String name;
    private final String[] values;

    /**
     * @param name
     *            the name of the parameter
     * @param values
     *            the list of values the parameter can assume
     * @param description
     *            the description of the parameter
     */
    public StringListParameter(final String name, final String[] values,
        final String description) {

        super(description);

        this.name = name;
        this.values = values;
        this.index = 0;
    }

    @Override
    public boolean advance() {

        if (this.hasMore()) {
            this.index++;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getDescription() {

        return this.description;
    }

    /**
     * @return the name of this parameter
     */
    public String getName() {

        return this.name;
    }

    /**
     * @return the value of this parameter
     */
    public String getValue() {

        return this.values[this.index];
    }

    @Override
    public boolean hasMore() {

        return this.index < this.values.length - 1;
    }

    @Override
    public String print() {

        return "-XX:" + this.getName() + "=" + this.getValue();
    }

    @Override
    public void reset() {

        this.index = 0;
    }

    @Override
    public String toString() {

        return this.print();
    }
}
