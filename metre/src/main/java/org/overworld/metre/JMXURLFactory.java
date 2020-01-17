

package org.overworld.metre;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.management.remote.JMXServiceURL;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

/**
 * Constructs a JMXServieURL that connects to localhost given the pid of the
 * target JVM or the port of a JMX service.
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class JMXURLFactory {

    private static final String CONNECTOR = "com.sun.management.jmxremote.localConnectorAddress";

    private static final String HOST = "localhost";

    /**
     * Construct a JMXServiceURL for a JVM running with a specified OS PID
     *
     * @param pid
     *            the process ID of the remote JVM in the operating system
     * @return a JMXServiceURL instance to connect to JMX on the JVM specified
     * @throws CommunicationException
     *             on error communicating with the remote JVM
     * @throws ApplicationException
     *             on error constructing the URL for the remote JMX
     */
    public static JMXServiceURL fromPid(final int pid)
        throws ApplicationException {

        String connectorAddress;

        VirtualMachine jvm;
        try {
            jvm = VirtualMachine.attach(Integer.toString(pid));

            connectorAddress = jvm.getAgentProperties().getProperty(
                JMXURLFactory.CONNECTOR);

            if (connectorAddress == null) {

                final String agent = jvm.getSystemProperties().getProperty(
                    "java.home")
                    + File.separator
                    + "lib"
                    + File.separator
                    + "management-agent.jar";

                jvm.loadAgent(agent);

                connectorAddress = jvm.getAgentProperties().getProperty(
                    JMXURLFactory.CONNECTOR);
                assert connectorAddress != null;
            }
        } catch (AttachNotSupportedException | IOException | AgentLoadException
            | AgentInitializationException e) {

            throw new CommunicationException(
                "Error querying remote JVM for port");
        }

        try {

            return new JMXServiceURL(connectorAddress);
        } catch (final MalformedURLException e) {

            throw new ApplicationException(
                "Unable to form URL for port on localhost");
        }
    }

    /**
     * Construct a JMXServiceURL for a JVM running on a specific port on the
     * localhost
     *
     * @param port
     *            the port on which the remote JVM's JMX is running
     * @return a JMXServiceURL instance to connect to JMX on the JVM specified
     * @throws ApplicationException
     *             on error constructing the URL for the remote JMX
     */
    public static JMXServiceURL fromPort(final int port)
        throws ApplicationException {

        if (port <= 0)
            throw new IllegalArgumentException(
                "Invalid port for connection to remote JVM: " + port);

        try {

            return new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + HOST + ":"
                + port + "/jmxrmi");
        } catch (final MalformedURLException e) {

            throw new ApplicationException(
                "Unable to form URL for port on localhost");
        }
    }

    /**
     * JMXUrlFactory has a private constructor as instantiation is redundant,
     * all methods are static.
     */
    private JMXURLFactory() {
    }
}