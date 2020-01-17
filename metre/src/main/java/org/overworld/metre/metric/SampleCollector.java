package org.overworld.metre.metric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.overworld.metre.ApplicationException;
import org.overworld.metre.CommunicationException;

/**
 * Collects MetricSlices from the MetricsFountain at specified intervals
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class SampleCollector implements Runnable {

    private static final int MAX_RETRIES = 5;

    /**
     * the minimum interval between samples. Requesting a sample interval less
     * than this results in an exception
     */
    public static final int MIN_INTERVAL = 100; /* ms */

    final private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

    private final MetricsFountain fountain;

    private final long interval;

    private final List<MetricSlice> slices = Collections
        .synchronizedList(new ArrayList<>());

    /**
     * Constructs an instance that collects the given desired metrics from the
     * given fountain at a specified interval
     *
     * @param desiredMetrics
     *            a Collection of MetricDescriptors describing all desired
     *            metrics
     * @param interval
     *            the duration between the start of collections
     * @param fountain
     *            the MetricsFountain to draw the metrics from
     */
    public SampleCollector(final long interval, final MetricsFountain fountain) {

        if (interval < MIN_INTERVAL)
            throw new IllegalArgumentException(String.format(
                "Specified interval %d is less than the minimum %d", interval,
                MIN_INTERVAL));

        this.fountain = fountain;
        this.interval = interval;
    }

    /**
     * @return the data slices collection consisting of all data gathered while
     *         this instance was started, as an unmodifialbe collection
     */
    public List<MetricSlice> getSlices() {

        final List<MetricSlice> result = Collections
            .unmodifiableList(new ArrayList<MetricSlice>(this.slices));
        this.slices.clear();
        return result;
    }

    /**
     * Performs the collection task at scheduled intervals with retry logic
     */
    @Override
    public void run() {

        int retry = 0;
        boolean done = false;
        while (!done && this.fountain.isConnected()) {
            try {

                this.slices.add(this.fountain.sample());
                done = true;
            } catch (final CommunicationException e) {

                if (++retry == MAX_RETRIES)
                    throw new RuntimeException(new ApplicationException(
                        "Retries exceeded attempting to obtain metrics from"
                            + " fountain", e));
            }
        }
    }

    /**
     * Starts metric collection
     *
     * @throws CommunicationException
     *             on error starting metrics collection
     */
    public void start() throws CommunicationException {

        this.fountain.connect();

        this.executor.scheduleAtFixedRate(this, 0, this.interval,
            TimeUnit.MILLISECONDS);
    }

    /**
     * Stops metric collection
     *
     * @throws CommunicationException
     *             on error stopping metrics collection
     */
    public void stop() throws CommunicationException {

        this.fountain.disconnect();

        this.executor.shutdownNow();
    }
}
