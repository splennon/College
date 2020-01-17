package org.overworld.metre.metric;

import javax.management.ObjectName;

import org.apache.log4j.Logger;

/**
 * Describes a JMX attribute contained within an MBean, herein referred to as a
 * metric. This class can be used to describe a concrete instance of a metric as
 * retrieved from a JMX instance or it can be partially populated to describe a
 * desired metric or set of desired metrics that match.
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class MetricDescriptor {

    private static Logger LOG = Logger.getLogger(MetricDescriptor.class);

    private final String location, type, subtype, metricName;

    private Class<?> valueType;

    /**
     * Constructs an instance populated appropriately from the objectName in the
     * format returned from
     * {@code javax.management.ObjectInstance#getObjectName()#toString()}
     * <p/>
     * This constructor is most useful for describing an attribute that has been
     * retrieved from a JMX server because the ObjectName is then definitely
     * known.
     * <p/>
     * An example ObjectName is: java.lang:type=MemoryPool,name=PS Old Gen
     *
     * @param objectName
     *            the ObjectName of the desired MBean in the format retrieved
     *            from ObjectInstance.getObjectName()
     * @param metricName
     *            the name of the attribute within the MBean
     * @param valueType
     *            the expected class of the value of the attributed identified
     */
    public MetricDescriptor(final ObjectName objectName, final String metricName) {

        if (metricName == null || metricName.equals(""))
            throw new IllegalArgumentException("A Metric Name is required");

        this.location = objectName.getDomain();
        this.type = objectName.getKeyProperty("type");
        this.subtype = objectName.getKeyProperty("name");

        this.metricName = metricName;
        this.valueType = null;
    }

    /**
     * Constructs an instance populated with the parameters specified.
     * <p/>
     * This constructor is most useful for describing a desired attribute as the
     * location (Such as java.lang) and the subtype can be omitted allowing this
     * instance to match multiple concrete metrics retrieved from a JMX instance
     * <p/>
     * In order to understand how these parameters relate to the
     * {@code javax.management.ObjectInstance#getObjectName()#toString()}
     * consider an example: <br/>
     * <code>java.lang:type=MemoryPool,name=PS Old Gen</code>
     * <p/>
     * This corresponds to the parameters: <br>
     * <code>*:type=(type),name=*</code>
     * <p/>
     * This is a convenience constructor where the location and subtype are
     * null, and thus match anything. The name attribute in ObjectName can be
     * nonexistant and will still match null
     *
     * @param type
     *            the type attribute as it would appear in the ObjectName string
     * @param metricName
     *            the name of the attribute within the MBean
     * @param valueType
     *            the expected class of the value of the attributed identified
     */
    public MetricDescriptor(final String type, final String metricName,
        final Class<?> valueType) {

        this(null, type, null, metricName, valueType);
    }

    /**
     * Constructs an instance populated with the parameters specified
     * <p/>
     * This constructor is most useful for describing a desired attribute as the
     * location (Such as java.lang) and the subtype can be omitted allowing this
     * instance to match multiple concrete metrics retrieved from a JMX instance
     * <p/>
     * In order to understand how these parameters relate to the
     * {@code javax.management.ObjectInstance#getObjectName()#toString()}
     * consider an example: <br/>
     * <code>java.lang:type=MemoryPool,name=PS Old Gen</code>
     * <p/>
     * This corresponds to the parameters: <br>
     * <code>(location):type=(type),name=(subtype)</code>
     *
     * @param location
     *            the package location as it would appear in the ObjectName
     *            string
     * @param type
     *            the type attribute as it would appear in the ObjectName string
     * @param subtype
     *            the name attribute, indicating the subtype of the MBean as it
     *            would appear in the ObjectName string
     * @param metricName
     *            the name of the attribute within the MBean
     * @param valueType
     *            the expected class of the value of the attributed identified
     */
    public MetricDescriptor(final String location, final String type,
        final String subtype, final String metricName, final Class<?> valueType) {

        if (metricName == null || metricName.equals(""))
            throw new IllegalArgumentException("A Metric Name is required");

        if (valueType == null)
            throw new IllegalArgumentException(
                "An expected value type is required with this constructor");

        this.location = location;
        this.type = type;
        this.subtype = subtype;
        this.metricName = metricName;
        this.valueType = valueType;
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        final MetricDescriptor other = (MetricDescriptor) obj;
        if (this.location == null) {
            if (other.location != null) return false;
        } else if (!this.location.equals(other.location)) return false;
        if (this.metricName == null) {
            if (other.metricName != null) return false;
        } else if (!this.metricName.equals(other.metricName)) return false;
        if (this.subtype == null) {
            if (other.subtype != null) return false;
        } else if (!this.subtype.equals(other.subtype)) return false;
        if (this.type == null) {
            if (other.type != null) return false;
        } else if (!this.type.equals(other.type)) return false;
        return true;
    }

    /**
     * location in the fashion of:
     * {@code javax.management.ObjectInstance#getObjectName()#toString()},
     * resembling: <br>
     * <code>(location):type=(type),name=(subtype)</code>
     *
     * @return the location field
     */
    public String getLocation() {

        return this.location;
    }

    /**
     * @return the name of the attribute within the MBean
     */
    public String getMetricName() {

        return this.metricName;
    }

    /**
     * subtype in the fashion of:
     * {@code javax.management.ObjectInstance#getObjectName()#toString()},
     * resembling: <br>
     * <code>(location):type=(type),name=(subtype)</code>
     *
     * @return the subtype field
     */
    public String getSubtype() {

        return this.subtype;
    }

    /**
     * type in the fashion of:
     * {@code javax.management.ObjectInstance#getObjectName()#toString()},
     * resembling: <br>
     * <code>(location):type=(type),name=(subtype)</code>
     *
     * @return the type field
     */
    public String getType() {

        return this.type;
    }

    /**
     * @return the expected type of the value or values identified by this
     *         instance
     */
    public Class<?> getValueType() {

        return this.valueType;
    }
    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((this.location == null) ? 0 : this.location.hashCode());
        result = prime * result
            + ((this.metricName == null) ? 0 : this.metricName.hashCode());
        result = prime * result
            + ((this.subtype == null) ? 0 : this.subtype.hashCode());
        result = prime * result
            + ((this.type == null) ? 0 : this.type.hashCode());
        return result;
    }

    /**
     * Checks if this instance matches the other descriptor. The other
     * descriptor is considered the specification and this instance is under
     * test. Any null values in the other descriptor match true by default.
     *
     * @param spec
     *            the other descriptor to act as a specification for comparison
     *            with this
     * @return true if this descriptor is correctly described by the other
     *         descriptor
     */
    public boolean matches(final MetricDescriptor spec) {

        if (this == spec) return true;

        if ((spec.location != null)
            && (this.location == null || !this.location.equals(spec.location)))
            return false;
        if ((spec.type != null)
            && (this.type == null || !this.type.equals(spec.type)))
            return false;
        if ((spec.subtype != null)
            && (this.subtype == null || !this.subtype.equals(spec.subtype)))
            return false;
        if ((spec.metricName != null)
            && (this.metricName == null || !this.metricName
            .equals(spec.metricName))) return false;
        if (spec.valueType != null && this.valueType != null
            && !spec.valueType.isAssignableFrom(this.valueType)) {
            LOG.warn(String.format(
                "Type %s is not assignable to %s for metric %s/%s/%s",
                this.valueType, spec.valueType, this.type, this.subtype,
                this.metricName));
            return false;
        }

        return true;
    }

    /**
     * @param valueType
     *            the expected type of the value or values identified by this
     *            instance
     */
    public void setValueType(final Class<?> valueType) {

        this.valueType = valueType;
    }

    @Override
    public String toString() {

        return "MetricDescriptor [location=" + this.location + ", type=" + this.type
            + ", subtype=" + this.subtype + ", metricName=" + this.metricName
            + ", valueType=" + this.valueType + "]";
    }
}

