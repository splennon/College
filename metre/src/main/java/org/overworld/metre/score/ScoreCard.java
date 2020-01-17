package org.overworld.metre.score;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.management.openmbean.CompositeDataSupport;

import org.apache.log4j.Logger;
import org.overworld.metre.metric.MetricDescriptor;
import org.overworld.metre.metric.MetricSample;
import org.overworld.metre.metric.MetricSlice;
import org.overworld.metre.process.JMeterResult;

/**
 * Records the score obtained for one test run and the data on which the score
 * is based for later graphing, representation or calculation
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-04-13
 */
public class ScoreCard implements Comparable<ScoreCard> {

    private final static DecimalFormat cpuFormatter = new DecimalFormat(
        "#.0000");

    private static Logger LOG = Logger.getLogger(ScoreCard.class);

    /**
     * a list of all GCs that can be used as new
     */
    private static final Set<String> NEW_GC = new TreeSet<String>(Arrays
        .asList(new String[] { "PS Scavenge", "ParNew", "G1 Young Generation",
        "Copy" }));

    /**
     * a list of all GCs that can be used as old
     */
    private static final Set<String> OLD_GC = new TreeSet<String>(Arrays
        .asList(new String[] { "ConcurrentMarkSweep", "G1 Old Generation",
            "MarkSweepCompact", "PS MarkSweep" }));

    private final List<Double> cpuLoad = new ArrayList<>();
    private final long cpuTime;
    private final List<Integer> elapsed = new ArrayList<>();
    private final int errTotal;
    private final List<Long> heapUsage = new ArrayList<>();
    private final String jvmArgs;
    private long majorCollectionCount = -1;
    private String majorCollectionName = "";
    private long majorCollectionTime = -1;
    private long minorCollectionCount = -1;
    private String minorCollectionName = "";
    private long minorCollectionTime = -1;
    private final List<Long> nonHeapUsage = new ArrayList<>();
    private final int runNumber;
    private final double runtime;
    private final List<Long> sampleTimes = new ArrayList<>();
    private final double score;

    /**
     * Construct a new scorecard to store the JMeter and JMX data, and JVM args
     * for this run
     *
     * @param slices
     *            a list of MetricSlices from which JMX metrics can be extracted
     * @param results
     *            JMeter results of the test run
     * @param jvmArgs
     *            the JVM args used to invoke the test run
     * @param runNumber
     *            an incrementing sequence number to identify the run
     */
    public ScoreCard(final List<MetricSlice> slices,
        final JMeterResult results, final String jvmArgs, final int runNumber) {

        this.jvmArgs = jvmArgs;
        this.runNumber = runNumber;

        /* data from the JMeter Result */

        this.runtime = results.getTotalTime();
        this.errTotal = results.getErr();

        /*
         * datapoints that are known to be cumulative are taken from the last
         * slice
         */

        final MetricSlice lastSlice = slices.get(slices.size() - 1);

        final List<MetricSample> cpuTimeResult = lastSlice
            .seek(new MetricDescriptor("OperatingSystem", "ProcessCpuTime",
                Long.class));

        if (cpuTimeResult.size() < 1) {
            LOG.error("Metric not found in result: ProcessCpuTime");
        }

        this.cpuTime = (Long) cpuTimeResult.get(0).getValue();

        for (final MetricSample ms : lastSlice.seek(new MetricDescriptor(
            "GarbageCollector", "CollectionTime", Long.class))) {

            final String subType = ms.getDescriptor().getSubtype();

            if (NEW_GC.contains(subType)) {

                this.minorCollectionTime = (Long) ms.getValue();
                this.minorCollectionName = subType;
            } else if (OLD_GC.contains(subType)) {

                this.majorCollectionTime = (Long) ms.getValue();
                this.majorCollectionName = subType;
            } else {

                LOG.error("Unknown GC SubType: " + subType);
            }
        }

        for (final MetricSample ms : lastSlice.seek(new MetricDescriptor(
            "GarbageCollector", "CollectionCount", Long.class))) {

            final String subType = ms.getDescriptor().getSubtype();

            if (NEW_GC.contains(subType)) {

                this.minorCollectionCount = (Long) ms.getValue();
            } else if (OLD_GC.contains(subType)) {

                this.majorCollectionCount = (Long) ms.getValue();
            } else {

                LOG.error("Unknown GC SubType: " + subType);
            }
        }

        /* now extract the timeseries data (not cumulative) */

        for (final MetricSlice slice : slices) {

            final List<MetricSample> sample = slice.seek(new MetricDescriptor(
                "Memory", "HeapMemoryUsage", CompositeDataSupport.class));

            if (sample.size() < 1) {
                LOG.error("Metric not found in result: HeapMemoryUsage");
            }

            this.heapUsage.add((Long) ((CompositeDataSupport) sample.get(0)
                .getValue()).get("used"));
        }

        for (final MetricSlice slice : slices) {

            final List<MetricSample> sample = slice.seek(new MetricDescriptor(
                "Memory", "NonHeapMemoryUsage", CompositeDataSupport.class));

            if (sample.size() < 1) {
                LOG.error("Metric not found in result: NonHeapMemoryUsage");
            }

            this.nonHeapUsage.add((Long) ((CompositeDataSupport) sample.get(0)
                .getValue()).get("used"));
        }

        for (final MetricSlice slice : slices) {

            final List<MetricSample> sample = slice.seek(new MetricDescriptor(
                "OperatingSystem", "ProcessCpuLoad", Double.class));

            if (sample.size() < 1) {
                LOG.error("Metric not found in result: ProcessCpuLoad");
            }

            this.cpuLoad.add((Double) sample.get(0).getValue());
        }

        MetricSlice previous = null;

        for (final MetricSlice slice : slices) {

            if (previous != null)
                this.elapsed.add((int) (slice.getTime() - previous.getTime()));

            this.sampleTimes.add(slice.getTime());
            previous = slice;
        }

        /*
         * for the purposes of this implementation the score is based on the
         * inverse of runtime with an adjustment for legibility. A score of 0 is
         * assigned if there were any JMeter test failures
         */

        if (results.getErr() > 0) {

            this.score = 0;
        } else {

            this.score = (1 / this.runtime) * 10000;
        }
    }

    /**
     * {@inheritDoc} Compares ScoreCard instances by the contained score value.
     */
    @Override
    public int compareTo(final ScoreCard other) {

        return Double.compare(this.getScore(), other.getScore());
    }

    /**
     * @return the CPU Load from JMX as time series
     */
    public List<Double> getCpuLoad() {

        return this.cpuLoad;
    }

    /**
     * @return the CPU Load from JMX as time series and formatted for injection
     *         into the GPlot output file
     */
    public String getCpuSeries() {

        int count = 1;
        final StringBuilder result = new StringBuilder();

        for (final double dp : this.cpuLoad) {

            result.append(count++ + " " + cpuFormatter.format(dp)
            + System.lineSeparator());
        }

        return result.toString();
    }

    /**
     * @return the accumulated CPU Time from JMX
     */
    public long getCpuTime() {

        return this.cpuTime;
    }

    /**
     * @return the total number of errors from JMeter
     */
    public int getErrTotal() {

        return this.errTotal;
    }

    /**
     * @return the Heap Usage from JMX as time series
     */
    public List<Long> getHeapUsage() {

        return this.heapUsage;
    }

    /**
     * @return the Heap Usage from JMX as time series and formatted for
     *         injection into the GPlot output file
     */
    public String getHeapUsageSeries() {

        int count = 1;
        final StringBuilder result = new StringBuilder();

        for (final long dp : this.heapUsage) {

            result.append(count++ + " " + dp + System.lineSeparator());
        }

        return result.toString();
    }

    /**
     * @return the JVM arguments used on invocation as a string
     */
    public String getJvmArgs() {

        return this.jvmArgs;
    }

    /**
     * @return the cumulative total number of GC Major Collections from JMX
     */
    public long getMajorCollectionCount() {

        return this.majorCollectionCount;
    }

    /**
     * @return the name of the Old Generation Garbage Collector from JMX
     */
    public String getMajorCollectionName() {

        return this.majorCollectionName;
    }

    /**
     * @return the time spent in Major GC from JMX
     */
    public long getMajorCollectionTime() {

        return this.majorCollectionTime;
    }

    /**
     * @return the cumulative total number of GC Minor Collections from JMX
     */
    public long getMinorCollectionCount() {

        return this.minorCollectionCount;
    }

    /**
     * @return the name of the New Generation Garbage Collector from JMX
     */
    public String getMinorCollectionName() {

        return this.minorCollectionName;
    }

    /**
     * @return the time spent in Minor GC from JMX
     */
    public long getMinorCollectionTime() {

        return this.minorCollectionTime;
    }

    /**
     * @return the Non-heap Usage from JMX as time series
     */
    public List<Long> getNonHeapUsage() {

        return this.nonHeapUsage;
    }

    /**
     * @return the Non-heap Usage from JMX as time series and formatted for
     *         injection into the GPlot output file
     */
    public String getNonHeapUsageSeries() {

        int count = 1;
        final StringBuilder result = new StringBuilder();

        for (final long dp : this.nonHeapUsage) {

            result.append(count++ + " " + dp + System.lineSeparator());
        }

        return result.toString();
    }

    /**
     * @return the arbitrary identifying run number
     */
    public int getRunNumber() {

        return this.runNumber;
    }

    /**
     * @return the cumulative runtime from JMeter
     */
    public double getRuntime() {

        return this.runtime;
    }

    /**
     * @return the duration between samples to validate the periodicity of the
     *         time series
     */
    public List<Long> getSampleTimes() {

        return this.sampleTimes;
    }

    /**
     * @return the calculated score
     */
    public double getScore() {

        return this.score;
    }

    @Override
    public String toString() {

        return "ScoreCard [cpuLoad=" + this.cpuLoad + ", cpuTime="
            + this.cpuTime + ", elapsed=" + this.elapsed + ", errTotal="
            + this.errTotal + ", heapUsage=" + this.heapUsage + ", jvmArgs="
            + this.jvmArgs + ", majorCollectionCount="
            + this.majorCollectionCount + ", majorCollectionName="
            + this.majorCollectionName + ", majorCollectionTime="
            + this.majorCollectionTime + ", minorCollectionCount="
            + this.minorCollectionCount + ", minorCollectionName="
            + this.minorCollectionName + ", minorCollectionTime="
            + this.minorCollectionTime + ", nonHeapUsage=" + this.nonHeapUsage
            + ", runtime=" + this.runtime + ", sampleTimes=" + this.sampleTimes
            + ", score=" + this.score + "]";
    }
}
