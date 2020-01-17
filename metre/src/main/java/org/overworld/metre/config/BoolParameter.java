package org.overworld.metre.config;

/**
 * Represents a dynamic parameter that can take on a value of true or false
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class BoolParameter extends Parameter {

    private final String name;
    private final boolean startingValue;
    private boolean value;

    /**
     * @param name
     *            the name of the parameter
     * @param startingValue
     *            the start of the range
     * @param description
     *            the description of the parameter
     */
    public BoolParameter(final String name, final boolean startingValue,
        final String description) {

        super(description);

        this.name = name;
        this.startingValue = startingValue;
        this.value = this.startingValue;
    }

    @Override
    public boolean advance() {

        if (this.hasMore()) {
            this.value = !this.value;
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the name of the parameter
     */
    public String getName() {

        return this.name;
    }

    /**
     * @return the current value of the parameter
     */
    public boolean getValue() {

        return this.value;
    }

    @Override
    public boolean hasMore() {

        return (this.value == this.startingValue);
    }

    @Override
    public String print() {

        return "-XX:" + (this.value ? "+" : "-") + this.getName();
    }

    @Override
    public void reset() {

        this.value = this.startingValue;
    }

    @Override
    public String toString() {

        return this.print();
    }
}
