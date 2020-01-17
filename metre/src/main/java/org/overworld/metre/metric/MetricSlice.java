package org.overworld.metre.metric;

import java.util.ArrayList;
import java.util.List;

/**
 * A collection of metrics all collected from the target JMX instance at the
 * same time
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class MetricSlice extends ArrayList<MetricSample> {

    private static final long serialVersionUID = 74926592L;

    private long time;

    /**
     * @return the time at which the MetricSamples in this MetricSlice were
     *         collected
     */
    public long getTime() {

        return this.time;
    }

    /**
     * @param interest
     *            the MetricDescriptor for which to search
     * @return all MetricSamples in this MetricSlice that match the
     *         MetricDescriptor given
     */
    public List<MetricSample> seek(final MetricDescriptor interest) {

        final ArrayList<MetricSample> results = new ArrayList<>();

        for (final MetricSample exists : this) {

            if (exists.getDescriptor().matches(interest)) {

                results.add(exists);
            }
        }

        return results;
    }

    /**
     * @param time
     *            the time at which the MetricSamples in this MetricSlice were
     *            collected
     */
    public void setTime(final long time) {

        this.time = time;
    }
}
