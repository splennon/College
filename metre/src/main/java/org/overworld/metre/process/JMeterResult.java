package org.overworld.metre.process;

/**
 * Represents the key statistics corresponding to a JMeter log summary line in
 * default format
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-03-05
 */
public class JMeterResult {

    private final double avg;
    private final int err;
    private final double errFactor;
    private final int max;
    private final int min;
    private final double testRate;
    private final int tests;
    private final double totalTime;

    /**
     * Construct an instance to store the immutable values specified as
     * parameters
     *
     * @param tests
     *            the number of tests run
     * @param totalTime
     *            the total time over all tests
     * @param testRate
     *            the rate at which tests ran in tests per second
     * @param avg
     *            the average test time in ms
     * @param min
     *            the minimum test time in ms
     * @param max
     *            the maximum time in ms
     * @param err
     *            the number of tests in error
     * @param errFactor
     *            the percentage of tests succeeded
     */
    public JMeterResult(final String tests, final String totalTime,
        final String testRate, final String avg, final String min,
        final String max, final String err, final String errFactor) {

        this.avg = Double.parseDouble(avg);
        this.err = Integer.parseInt(err);
        this.max = Integer.parseInt(max);
        this.min = Integer.parseInt(min);
        this.tests = Integer.parseInt(tests);
        this.errFactor = Double.parseDouble(errFactor);
        this.testRate = Double.parseDouble(testRate);
        this.totalTime = Double.parseDouble(totalTime);
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        final JMeterResult other = (JMeterResult) obj;
        if (Double.doubleToLongBits(this.avg) != Double.doubleToLongBits(other.avg))
            return false;
        if (this.err != other.err) return false;
        if (this.max != other.max) return false;
        if (this.min != other.min) return false;
        if (Double.doubleToLongBits(this.errFactor) != Double
            .doubleToLongBits(other.errFactor)) return false;
        if (Double.doubleToLongBits(this.testRate) != Double
            .doubleToLongBits(other.testRate)) return false;
        if (this.tests != other.tests) return false;
        if (Double.doubleToLongBits(this.totalTime) != Double
            .doubleToLongBits(other.totalTime)) return false;
        return true;
    }

    /**
     * @return the factor of tests that succeeded
     */
    public double gerErrFactor() {

        return this.errFactor;
    }

    /**
     * @return the average duration over all tests run in ms
     */
    public double getAvg() {

        return this.avg;
    }

    /**
     * @return the number of tests in error
     */
    public int getErr() {

        return this.err;
    }

    /**
     * @return the maximum duration over all tests run in ms
     */
    public int getMax() {

        return this.max;
    }

    /**
     * @return the minimum duration over all tests run in ms
     */
    public int getMin() {

        return this.min;
    }

    /**
     * @return the rate at which tests ran in tests per second
     */
    public double getTestRate() {

        return this.testRate;
    }

    /**
     * @return the total number of tests that ran
     */
    public int getTests() {

        return this.tests;
    }

    /**
     * @return the total wall clock time for which the tests ran in seconds
     */
    public double getTotalTime() {

        return this.totalTime;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(this.avg);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + this.err;
        result = prime * result + this.max;
        result = prime * result + this.min;
        temp = Double.doubleToLongBits(this.errFactor);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.testRate);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + this.tests;
        temp = Double.doubleToLongBits(this.totalTime);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {

        return "JMeterResult [avg=" + this.avg + ", err=" + this.err + ", max="
            + this.max + ", min=" + this.min + ", errFactor=" + this.errFactor
            + ", testRate=" + this.testRate + ", tests=" + this.tests
            + ", totalTime=" + this.totalTime + "]";
    }
}
