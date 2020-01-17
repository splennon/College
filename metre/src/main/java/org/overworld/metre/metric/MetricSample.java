package org.overworld.metre.metric;

import java.util.Arrays;

/**
 * A class to hold a MetricDescriptor along with its associated value for any
 * given sample
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class MetricSample {

    private MetricDescriptor descriptor;
    private Object value;

    /**
     * Construct an instance to hold the specified descriptor
     *
     * @param descriptor
     *            the MetricDescriptor to store
     */
    public MetricSample(final MetricDescriptor descriptor) {

        this.setDescriptor(descriptor);
    }

    /**
     * Construct an instance to hold the specified descriptor and value
     *
     * @param descriptor
     *            the MetricDescriptor to store
     * @param value
     *            the specified value to store
     */
    public MetricSample(final MetricDescriptor descriptor, final Object value) {

        this.setDescriptor(descriptor);
        this.setValue(value);
    }

    /**
     * {@inheritDoc}}
     */
    @Override
    public boolean equals(final Object obj) {

        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        final MetricSample other = (MetricSample) obj;
        if (this.descriptor == null) {
            if (other.descriptor != null) return false;
        } else if (!this.descriptor.equals(other.descriptor)) return false;
        if (this.value == null) {
            if (other.value != null) return false;
        } else if (!this.value.equals(other.value)) return false;
        return true;
    }

    /**
     * @return the MetricDescriptor in this instance
     */
    public MetricDescriptor getDescriptor() {

        return this.descriptor;
    }

    /**
     * @return the value object in this instance
     */
    public Object getValue() {

        return this.value;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((this.descriptor == null) ? 0 : this.descriptor.hashCode());
        result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
        return result;
    }

    /**
     * @param descriptor
     *            the MetricDescriptor to set in this instance
     */
    public void setDescriptor(final MetricDescriptor descriptor) {

        this.descriptor = descriptor;
    }

    /**
     * @param value
     *            the value object to set in this instance
     */
    public void setValue(final Object value) {

        this.value = value;
    }

    @Override
    public String toString() {

        if (this.value == null) {
            return "MetricSample [descriptor=" + this.descriptor + ", value="
                + this.value + " ]\n";
        } else {

            String valueString;
            if (this.value instanceof Object[]) {
                valueString = Arrays.deepToString((Object[]) this.value);
            } else {
                valueString = this.value.toString();
            }

            return "MetricSample [descriptor=" + this.descriptor + ", value="
            + valueString + ", class=" + this.value.getClass() + " ]\n";
        }
    }
}
