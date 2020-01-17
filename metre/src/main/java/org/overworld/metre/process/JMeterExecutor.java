package org.overworld.metre.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.overworld.metre.ApplicationException;
import org.overworld.metre.CommunicationException;
import org.overworld.metre.config.JMeter;

/**
 * Executes a JMeter process from defined properties and parses the results for
 * key statistics of the test afterward
 * <p/>
 * @formatter:off
 * jmeter.java        the java executable to use
 * jmeter.initialHeap the initial heap size for the JMeter process
 * jmeter.maxHeap     the maximum heap size for the JMeter process
 * jmeter.mainJar     the Apache JMeter jar, including relative path from
 *                      the working directory if required
 * jmeter.testFile    the JMeter test plan, including relative path from
 *                      the working directory if required
 * jmeter.logFile     the JMeter log, including relative path from
 *                      the working directory if required
 * <p/>
 * The JMeter log file is not used by this implementation, preferring instead
 * the standard summary output format written by default to standard output of
 * JMeter
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-03-06
 */
public class JMeterExecutor {

    private static Logger LOG = Logger.getLogger(JMeterExecutor.class);

    /**
     * A Pattern that matches the cumulative output line of the JMeter log
     */
    public static Pattern SUMMARY_LINE_PATTERN = Pattern
        .compile("^\\s*summary\\s+=\\s+(\\d+)\\s+in\\s+([\\d.]+)s\\s+=\\s+([\\d.]+)/s\\s+Avg:\\s+(\\d+)\\s+Min:\\s+(\\d+)\\s+Max:\\s+(\\d+)\\s+Err:\\s+(\\d+)\\s+\\(([\\d.]+)%\\)");

    private final List<String> command = new ArrayList<>();
    private Process process;

    /**
     * Constructs a JMeter process but does not start it. The process may be
     * started many times over, though not concurrently, so long as the methods
     * {@link JMeterExecutor#reap()}, {@link JMeterExecutor#reap()} and
     * {@link JMeterExecutor#results()} are called in order before reusing the
     * instance
     *
     * @param props
     *            the {@link java.util.Properties} representation of the
     *            jmeter.properties file
     * @throws ApplicationException
     *             in the event that require properties are missing
     */
    public JMeterExecutor(final JMeter props) throws ApplicationException {

        this.command.add(props.getJava());
        this.command.add(String.format("-Xms%sM", props.getInitialHeap()));
        this.command.add(String.format("-Xmx%sM", props.getMaxHeap()));

        this.command.add("-jar");
        this.command.add(props.getMainJar());

        this.command.add("-n");

        this.command.add("-t");
        this.command.add(props.getTestFile());

        this.command.add("-l");
        this.command.add(props.getLogFile());

        LOG.debug("Constructed JMeterExecutor with command: "
            + this.command.toString());
    }

    /**
     * Wait for termination of the process and get the OS return code
     *
     * @return true if the OS return code as 0, meaning success, false otherwise
     */
    public boolean reap() {

        while (true) {
            try {
                final int rcode = this.process.waitFor();
                return (rcode == 0);
            } catch (final InterruptedException e) {
                LOG.debug("Interrupted wiating for JMeter process");
            }
        }
    }

    /**
     * Parses the output of the JMeter test run and returns a JMeterResult
     * object populated with the values of the last cumulative summary line
     * which represents the results of the entire test run
     *
     * @return the results of the test run as a JMeterResult instance or null if
     *         no parsable cumulative summary was found
     * @throws IOException
     *             on error reading the output of the command
     * @throws ApplicationException
     */
    public JMeterResult results() throws ApplicationException {

        final BufferedReader output = new BufferedReader(new InputStreamReader(
            this.process.getInputStream()));

        /*
         * the default and expected format for a JMeter log summary line is
         * this: summary = 2 in 1.2s = 1.7/s Avg: 20 Min: 6 Max: 35 Err: 1
         * (50.00%)
         */

        JMeterResult result = null;

        String line = "";

        try {

            while ((line = output.readLine()) != null) {

                final Matcher match = SUMMARY_LINE_PATTERN.matcher(line);

                if (match.matches()) {

                    try {

                        result = new JMeterResult(match.group(1), match.group(2),
                            match.group(3), match.group(4), match.group(5), match
                            .group(6), match.group(7), match.group(8));
                    } catch (final NumberFormatException e) {

                        /* @formatter:off */

                        throw new ApplicationException(String.format(
                            "Cannot parse results from JMeter summary:"
                                + " tests: %s total time: %s test rate: %s"
                                + " average: %s minimum: %s maimum:  %s"
                                + " errors: %s successful: %s%%",
                                match.group(1),
                                match.group(2),
                                match.group(3),
                                match.group(4),
                                match.group(5),
                                match.group(6),
                                match.group(7)
                            ), e);

                        /* @formatter:on */
                    }
                }
            }
        } catch (final IOException e) {

            throw new CommunicationException(
                "Error reading from JMeter log file", e);
        }

        this.process = null;

        return result;
    }

    /**
     * Starts the JMeter process
     *
     * @throws CommunicationException
     *             on error starting the process
     */
    public void start() throws CommunicationException {

        final ProcessBuilder processBuilder = new ProcessBuilder(this.command);
        processBuilder.redirectErrorStream(true);

        try{

            this.process = processBuilder.start();
        } catch (final IOException e) {

            throw new CommunicationException(
                "Error starting OS process for JMeter", e);
        }
    }

    @Override
    public String toString() {

        return "JMeterExecutor [command=" + this.command + ", process="
            + this.process + "]";
    }
}
