package org.overworld.metre.config;

/**
 * A POJO to represent the values of the MetreOptions section of the
 * configuration file
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class MetreOptions {

    private int sampleInterval;
    private int topResults;

    /**
     * @return the interval between samples
     */
    public int getSampleInterval() {

        return this.sampleInterval;
    }

    /**
     * @return the number of top results for which to generate GPlot outputs
     */
    public int getTopResults() {

        return this.topResults;
    }

    /**
     * @param sampleInterval
     *            the interval between samples
     * @return this reference for chaining
     */
    public MetreOptions setSampleInterval(final int sampleInterval) {

        this.sampleInterval = sampleInterval;
        return this;
    }

    /**
     * @param topResults
     *            the number of top results for which to generate GPlot outputs
     * @return this reference for chaining
     */
    public MetreOptions setTopResults(final int topResults) {

        this.topResults = topResults;
        return this;
    }
}
