package org.overworld.metre.config;

/**
 * A POJO to represent the values of the Target section of the configuration
 * file
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class Target {

    private String arguments;
    private String invocation;
    private String java;
    private int jmxPort;
    private String jvmParams;

    /**
     * @return command line arguments to the target application
     */
    public String getArguments() {

        return this.arguments;
    }

    /**
     * @return the Jar file to invoke, absolute or relative
     */
    public String getInvocation() {

        return this.invocation;
    }

    /**
     * @return the java binary to use, absolute or relative
     */
    public String getJava() {

        return this.java;
    }

    /**
     * @return the port on which the target will expose JMX
     */
    public int getJmxPort() {

        return this.jmxPort;
    }

    /**
     * @return JVM parameters to pass to the target JVM in addition to those
     *         that form part of the experiment
     */
    public String getJvmParams() {

        return this.jvmParams;
    }

    /**
     * @param arguments
     *            command line arguments to the target application
     * @return this reference for chaining
     */
    public Target setArguments(final String arguments) {

        this.arguments = arguments;
        return this;
    }

    /**
     *
     * @param invocation
     *            the Jar file to invoke, absolute or relative
     * @return this reference for chaining
     */
    public Target setInvocation(final String invocation) {

        this.invocation = invocation;
        return this;
    }

    /**
     * @param java
     *            the java binary to use, absolute or relative
     * @return this reference for chaining
     */
    public Target setJava(final String java) {

        this.java = java;
        return this;
    }

    /**
     * @param jmxPort
     *            the port on which the target will expose JMX
     * @return this reference for chaining
     */
    public Target setJmxPort(final int jmxPort) {

        this.jmxPort = jmxPort;
        return this;
    }

    /**
     * @param jvmParams
     *            JVM parameters to pass to the target JVM in addition to those
     *            that form part of the experiment
     * @return this reference for chaining
     */
    public Target setJvmParams(final String jvmParams) {

        this.jvmParams = jvmParams;
        return this;
    }

    @Override
    public String toString() {

        return "Target [arguments=" + this.arguments + ", invocation="
            + this.invocation + ", java=" + this.java + ", jmxPort="
            + this.jmxPort + ", jvmParams=" + this.jvmParams + "]";
    }
}
