package org.overworld.metre.config;

/**
 * Represents a dynamic parameter that can take on a value from a sequence of
 * integers defined by start and end value, and incremental step.
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class IntegralStepParameter extends Parameter {

    private long increment;
    private long maxValue;
    private long minValue;
    private String name;
    private long value;

    /**
     * @param name
     *            the name of the parameter
     * @param minValue
     *            the minimum value of the range
     * @param maxValue
     *            the maximum value of the range
     * @param increment
     *            the incremental step
     * @param description
     *            the description of the parameter
     */
    public IntegralStepParameter(final String name, final long minValue,
        final long maxValue, final int increment, final String description) {

        super(description);

        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = minValue;
        this.increment = increment;
        this.name = name;
    }

    @Override
    public boolean advance() {

        if (this.hasMore()) {
            this.value += this.increment;
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
     * @return the value of the incremental step
     */
    public long getIncrement() {

        return this.increment;
    }

    /**
     * @return the maximum value in the range
     */
    public long getMaxValue() {

        return this.maxValue;
    }

    /**
     * @return the minimum value in the range
     */
    public long getMinValue() {

        return this.minValue;
    }

    /**
     * @return the name of the parameter
     */
    public String getName() {

        return this.name;
    }

    /**
     * @return the current value in the sequence
     */
    public long getValue() {

        return this.value;
    }

    @Override
    public boolean hasMore() {

        return (this.value + this.increment <= this.maxValue);
    }

    @Override
    public String print() {

        return "-XX:" + this.getName() + "=" + this.getValue();
    }

    @Override
    public void reset() {

        this.value = this.minValue;
    }

    /**
     * @param increment
     *            the value of the increment in the range
     */
    public void setIncrement(final long increment) {

        this.increment = increment;
    }

    /**
     * @param maxValue
     *            the maximum value in the range
     */
    public void setMaxValue(final long maxValue) {

        this.maxValue = maxValue;
    }

    /**
     * @param minValue
     *            the minimum value in the range
     */
    public void setMinValue(final long minValue) {

        this.minValue = minValue;
    }

    /**
     * @param name
     *            the name of the parameter
     */
    public void setName(final String name) {

        this.name = name;
    }

    @Override
    public String toString() {

        return this.print();
    }
}
