package org.overworld.metre.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.overworld.metre.ApplicationException;
import org.overworld.metre.CommunicationException;
import org.overworld.metre.ProcessException;
import org.overworld.metre.config.Target;

/**
 * Manages the target process that is the subject of the performance test
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-03-06
 */
public class TargetExecutor {

    private static final int KILL_DELAY = 5000;
    private static Logger LOG = Logger.getLogger(TargetExecutor.class);
    public static Pattern WAIT_TEXT_MATCHER = Pattern
        .compile(".*Started Application.*");
    private static List<String> splitToList(final String toSplit) {

        return Arrays.asList(toSplit.split("\\s+"));
    }

    private final List<String> command = new ArrayList<>();
    private BufferedReader output;

    private Process process;

    /**
     * Constructs a target process but does not start it. The process may be
     * started many times over, though not concurrently, so long as the methods
     * {@link TargetExecutor#reap()}, {@link JMeterExecutor#reap()} and
     * {@link TargetExecutor#results()} are called in order before reusing the
     * instance
     *
     * @param props
     *            the {@link java.util.Properties} representation of the
     *            target.properties file
     * @param jvmOpts
     *            the collection of jvm options for this invocation
     * @throws ApplicationException
     *             in the event that require properties are missing
     */
    public TargetExecutor(final Target props, final String jvmOpts)
        throws CommunicationException {

        this.command.add(props.getJava());
        this.command.addAll(splitToList(props.getJvmParams()));

        this.command.addAll(splitToList(jvmOpts));

        this.command.add("-jar");
        this.command.add(props.getInvocation());
        this.command.addAll(splitToList(props.getArguments()));

        LOG.debug("Constructed TargetExecutor with command: "
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
                LOG.debug("Interrupted wiating for Target process");
            }
        }
    }

    /**
     * Starts the Target process and waits for it to print a line matching the
     * WAIT_TEXT_PATTERN
     *
     * @return true if the process produces its Wait Text prior to termination,
     *         false otherwise
     * @throws CommunicationException
     *             on error starting the process or reading its output
     */
    public boolean start() throws CommunicationException {

        final ProcessBuilder processBuilder = new ProcessBuilder(this.command);
        processBuilder.redirectErrorStream(true);

        try {

            this.process = processBuilder.start();
        } catch (final IOException e) {

            throw new CommunicationException(
                "Error starting OS process for target", e);
        }

        this.output = new BufferedReader(new InputStreamReader(this.process
            .getInputStream()));

        Matcher match;
        String line = null;

        do {

            try {

                line = this.output.readLine();
            } catch (final IOException e) {

                throw new CommunicationException(
                    "Error reading output from target", e);
            }

            if (line == null) return false;
            LOG.trace(line);
            match = WAIT_TEXT_MATCHER.matcher(line);
        } while (!match.matches());

        return true;
    }

    /**
     * Stop the target process
     *
     * @throws ProcessException
     *             if an error occurs stopping the process
     */
    public void stop() throws ProcessException {

        this.process.destroy();

        long timeout = System.currentTimeMillis() + KILL_DELAY;
        do {
            if (!this.process.isAlive()) return;
        } while (System.currentTimeMillis() < timeout);

        /*
         * if we get to here, the process is alive KILL_DELAY ms after
         * destroy(), so kill more forcibly.
         */

        this.process.destroyForcibly();

        timeout = System.currentTimeMillis() + KILL_DELAY;
        do {
            if (!this.process.isAlive()) return;
        } while (System.currentTimeMillis() < timeout);

        /*
         * if we get to here, the process is alive KILL_DELAY ms after
         * destroyForcibly() and that is an error.
         */

        throw new ProcessException(String.format("Process will not die after "
            + "destroyForcibly is called, waited a total of %d seconds",
            KILL_DELAY * 2));
    }

    @Override
    public String toString() {

        return "TargetExecutor [command=" + this.command + ", output="
            + this.output + ", process=" + this.process + "]";
    }
}
