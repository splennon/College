package org.overworld.metre.metric;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;
import org.overworld.metre.CommunicationException;

/**
 * Allows connection to a remote JMX instance and sampling of JMX attributes
 * from the JMX MBeans on the remote JVM.
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class MetricsFountain {

    private static Logger LOG = Logger.getLogger(MetricsFountain.class);
    private boolean connected;
    private JMXConnector connector;
    private final Collection<MetricDescriptor> desiredMetrics;
    private MBeanServerConnection remote;
    private final Map<ObjectName, List<String>> remoteMBeans = new HashMap<>();
    private final JMXServiceURL targetJMX;

    /**
     * Constructs a new MetricsFountain ready to connect to the target
     * JMXServiceURL
     *
     * @param targetJMX
     *            the JMXServiceURL describing the JMX instance to which to
     *            connect
     * @param desiredMetrics
     *            the collection of MetricDescriptors to collect from the remote
     *            JVM
     */
    public MetricsFountain(final JMXServiceURL targetJMX,
        final Collection<MetricDescriptor> desiredMetrics) {

        this.desiredMetrics = desiredMetrics;
        this.targetJMX = targetJMX;
        this.connected = false;
    }

    /**
     * Attempt to connect this instance to its remote JMX
     *
     * @throws CommunicationException
     *             on error connecting or communicating with the remote JMX
     *             instance
     */
    public void connect() throws CommunicationException {

        try {

            this.connector = JMXConnectorFactory.connect(this.targetJMX);
            this.remote = this.connector.getMBeanServerConnection();

            this.remoteMBeans.clear();

            for (final ObjectInstance remoteMBean : this.remote.queryMBeans(
                null, null)) {

                final MBeanInfo beanInfo = this.remote.getMBeanInfo(remoteMBean
                    .getObjectName());
                final ObjectName objectName = remoteMBean.getObjectName();
                final String[] attributes = Arrays.stream(
                    beanInfo.getAttributes()).map(mbai -> mbai.getName())
                    .toArray(String[]::new);

                FOREACHATTRIBUTE: for (final String attr : attributes) {

                    final MetricDescriptor comparison = new MetricDescriptor(
                        objectName, attr);

                    for (final MetricDescriptor interest : this.desiredMetrics) {

                        if (comparison.matches(interest)) {

                            List<String> selected;
                            while ((selected = this.remoteMBeans
                                .get(objectName)) == null) {
                                this.remoteMBeans.put(objectName,
                                    new ArrayList<String>());
                            }

                            selected.add(attr);
                            continue FOREACHATTRIBUTE;
                        }
                    }
                }
            }
        } catch (final IOException e) {
            throw new CommunicationException("Error connecting to remote JMX",
                e);
        } catch (InstanceNotFoundException | IntrospectionException
            | ReflectionException e) {
            throw new CommunicationException("Error querying remote JMX", e);
        }

        if (LOG.isInfoEnabled()) {
            for (final Entry<ObjectName, List<String>> entry : this.remoteMBeans
                .entrySet()) {
                LOG.info(String.format(
                    "Bean %s contributes the attributes named %s", entry
                    .getKey().getCanonicalName(), Arrays.toString(entry
                        .getValue().toArray())));
            }
        }

        this.connected = true;
    }

    /**
     * Disconnects from the remote JMX instance
     *
     * @throws CommunicationException
     *             on error disconnecting from the remote JMX instance
     */
    public void disconnect() throws CommunicationException {

        this.connected = false;

        try {
            this.connector.close();
        } catch (final IOException e) {
            throw new CommunicationException(
                "Error disconnecting from remote JMX", e);
        }
    }

    /**
     * @return true if this instance is connected to a remote JMX instance,
     *         false otherwise
     */
    public boolean isConnected() {

        return this.connected;
    }

    /**
     * Queries the remote JMX instance and retrieves values for all desired
     * attributes.
     * <p/>
     * Note that one MetricDescritpor might result in the selection of multiple
     * values, such as, for example, if MemoryPool is selected but the name of
     * the MemoryPool is omitted, then the specified metric will be retrieved
     * for every MemoryPool.
     * <p/>
     * MetricDescriptors in the return value however have all descriptive
     * members populated.
     *
     * @return a MetricSlice containing all MetricDescriptors and the current
     *         value for the corresponding JMX attribute where it matches any
     *         descriptorOfInterest
     * @throws CommunicationException
     *             on error communicating with the remote JMX instance
     */
    public MetricSlice sample() throws CommunicationException {

        try {

            if (!this.connected)
                throw new IllegalStateException(
                    "Not connected to remote JMX instance");

            final MetricSlice metrics = new MetricSlice();
            metrics.setTime(System.currentTimeMillis());

            for (final Entry<ObjectName, List<String>> entry : this.remoteMBeans
                .entrySet()) {

                final ObjectName objectName = entry.getKey();
                final List<String> attributes = entry.getValue();

                List<Attribute> attrList;
                try {
                    attrList = this.remote.getAttributes(objectName,
                        attributes.toArray(new String[] {})).asList();
                } catch (InstanceNotFoundException | ReflectionException
                    | IOException e) {
                    throw new CommunicationException(
                        "Error retrieving attributes from remote JMX instance", e);
                }

                for(final Attribute attr : attrList) {

                    final MetricDescriptor descriptor = new MetricDescriptor(
                        objectName, attr.getName());

                    if (attr.getValue() != null)
                        descriptor.setValueType(attr.getValue().getClass());

                    metrics.add(new MetricSample(descriptor, attr.getValue()));

                    LOG.debug(String.format("Collected: %s/%s/%s = %s", descriptor
                        .getType(),
                        descriptor.getSubtype(), descriptor.getMetricName(), attr
                        .getValue()));
                }
            }

            return metrics;
        } catch (final Throwable t) {

            /*
             * because this method runs in an executor pool, which may mask
             * exceptions, and because catching an exception from the code that
             * has not been accounted for indicates a serious error here, the
             * exception is both logged and re-trhown, in deliberate
             * contravention of the Log-and-throw antipattern. Throwable is used
             * so we can catch and log internal JMV errors as well as
             * exceptions.
             */

            LOG.error("Unhandled exception reading from remote JMX", t);
            throw t;
        }
    }
}
