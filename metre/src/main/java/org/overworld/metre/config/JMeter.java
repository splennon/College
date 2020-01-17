package org.overworld.metre.config;

/**
 * A POJO to represent the values of the JMeter section of the configuration
 * file
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class JMeter {

    private String initialHeap;
    private String java;
    private String logFile;
    private String mainJar;
    private String maxHeap;
    private String testFile;

    /**
     * @return the initial heap in MB for JMeter
     */
    public String getInitialHeap() {

        return this.initialHeap;
    }

    /**
     * @return the java binary to use, absolute or relative
     */
    public String getJava() {

        return this.java;
    }

    /**
     * @return the name of the JMeter log file
     */
    public String getLogFile() {

        return this.logFile;
    }

    /**
     * @return the location of the Jar file for Apache JMeter
     */
    public String getMainJar() {

        return this.mainJar;
    }

    /**
     * @return the maximum heap setting in MB for JMeter
     */
    public String getMaxHeap() {

        return this.maxHeap;
    }

    /**
     * @return the JMeter test file to run
     */
    public String getTestFile() {

        return this.testFile;
    }

    /**
     * @param initialHeap
     *            the initial heap in MB for JMeter
     * @return this reference for chaining
     */
    public JMeter setInitialHeap(final String initialHeap) {

        this.initialHeap = initialHeap;
        return this;
    }

    /**
     * @param java
     *            the java binary to use, absolute or relative
     * @return this reference for chaining
     */
    public JMeter setJava(final String java) {

        this.java = java;
        return this;
    }

    /**
     * @param logFile
     *            the name of the JMeter log file
     * @return this reference for chaining
     */
    public JMeter setLogFile(final String logFile) {

        this.logFile = logFile;
        return this;
    }

    /**
     * @param mainJar
     *            the location of the Jar file for Apache JMeter
     * @return this reference for chaining
     */
    public JMeter setMainJar(final String mainJar) {

        this.mainJar = mainJar;
        return this;
    }

    /**
     * @param maxHeap
     *            the maximum heap setting in MB for JMeter
     * @return this reference for chaining
     */
    public JMeter setMaxHeap(final String maxHeap) {

        this.maxHeap = maxHeap;
        return this;
    }

    /**
     * @param testFile
     *            the JMeter test file to run
     * @return this reference for chaining
     */
    public JMeter setTestFile(final String testFile) {

        this.testFile = testFile;
        return this;
    }

    @Override
    public String toString() {

        return "JMeter [initialHeap=" + this.initialHeap + ", java="
            + this.java + ", logFile=" + this.logFile + ", mainJar="
            + this.mainJar + ", maxHeap=" + this.maxHeap + ", testFile="
            + this.testFile + "]";
    }
}
