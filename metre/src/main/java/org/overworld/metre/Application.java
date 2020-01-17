package org.overworld.metre;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;
import org.overworld.metre.config.ConfigRoot;
import org.overworld.metre.config.MetreConfig;
import org.overworld.metre.metric.MetricDescriptor;
import org.overworld.metre.metric.MetricsFountain;
import org.overworld.metre.metric.SampleCollector;
import org.overworld.metre.process.JMeterExecutor;
import org.overworld.metre.process.TargetExecutor;
import org.overworld.metre.score.ScoreCard;
import org.overworld.metre.score.ScoreTable;

/**
 * The main class for the Metre application. Provides main() entry point and
 * high level application logic.
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class Application {

    private static Logger LOG = Logger.getLogger(Application.class);

    /**
     * @param args
     *            unused as all configuration comes from ./Config.xml
     * @throws CommunicationException
     *             on error communicating via JMX or with target or JMeter
     *             processes
     * @throws ApplicationException
     *             on internal non-transient error including those resulting
     *             from configuration and file IO
     */
    public static void main(final String[] args) throws CommunicationException,
    ApplicationException {

        final Application app = new Application();
        app.testCycle();
    }

    private List<String> alternateJvmArgs;
    private final ConfigRoot configRoot;
    final Collection<MetricDescriptor> desiredMetrics = new ArrayList<>();
    private final ScoreTable scoreTable;

    /**
     * Construct a new application instance.
     *
     * @throws CommunicationException
     *             on error communicating via JMX or with target or JMeter
     *             processes
     * @throws ApplicationException
     *             on internal non-transient error including those resulting
     *             from configuration and file IO
     */
    public Application() throws CommunicationException, ApplicationException {

        /*
         * Construct a new score table to store score cards from this experiment
         */
        this.scoreTable = new ScoreTable();

        /* get the application configuration */

        this.configRoot = (new MetreConfig()).fromFile();

        /* specify the metrics that are desired from the remote JMX */

        this.desiredMetrics.add(new MetricDescriptor("GarbageCollector",
            "CollectionCount", Long.class));
        this.desiredMetrics.add(new MetricDescriptor("GarbageCollector",
            "CollectionTime", Long.class));

        this.desiredMetrics.add(new MetricDescriptor("Memory", "HeapMemoryUsage",
            CompositeDataSupport.class));
        this.desiredMetrics.add(new MetricDescriptor("Memory", "NonHeapMemoryUsage",
            CompositeDataSupport.class));

        this.desiredMetrics.add(new MetricDescriptor("OperatingSystem",
            "ProcessCpuTime", Long.class));
        this.desiredMetrics.add(new MetricDescriptor("OperatingSystem",
            "ProcessCpuLoad", Double.class));
    }

    /**
     * Performs an entire test run using the desired metrics, application
     * configuration and scoreTable in this instance.
     *
     * @throws CommunicationException
     *             on error communicating via JMX or with target or JMeter
     *             processes
     * @throws ApplicationException
     *             on internal non-transient error including those resulting
     *             from configuration and file IO
     */
    public void testCycle() throws CommunicationException,
        ApplicationException {

        this.alternateJvmArgs = this.configRoot.expand();

        /* point the metrics fountain at the target */

        final JMXServiceURL target = JMXURLFactory.fromPort(this.configRoot
            .getTargetOptions().getJmxPort());

        /* initialise the metrics fountain */

        final MetricsFountain mf = new MetricsFountain(target,
            this.desiredMetrics);

        int countRuns = 1;

        /*
         * iterate through all JVM parameter combinations from the application
         * configuration
         */

        for (final String alternateJvmArgString : this.alternateJvmArgs) {

            /*
             * ready the TargetExecutor to run the experiment target
             * application
             */

            final TargetExecutor tex = new TargetExecutor(this.configRoot
                .getTargetOptions(), alternateJvmArgString);

            final SampleCollector collect = new SampleCollector(
                this.configRoot.getMetreOptions().getSampleInterval(), mf);

            /*
             * ready the JMeter process to measure this iteration of the
             * experiment
             */

            final JMeterExecutor jmex = new JMeterExecutor(this.configRoot
                .getJmeterOptions());

            try {

                /*
                 * run the target application, this call blocks until the
                 * application prints its ready string
                 */
                tex.start();

                /* start the JMX collector */

                collect.start();

                /*
                 * start the JMeter process to apply load to the target and wait
                 * until the test script is finished
                 */

                jmex.start();
                jmex.reap();

                /* stop the JMX collector */

                collect.stop();

                /* stop the target */

                tex.stop();
            } catch (final ApplicationException e) {

                LOG.error("Failed test iteration", e);
            }

            /* add the results of this test run to the score table */

            this.scoreTable.add(new ScoreCard(collect.getSlices(), jmex
                .results(), alternateJvmArgString, countRuns++));
        }

        /* dump the top n results to the output directory */

        this.scoreTable.dumpTop(this.configRoot.getMetreOptions()
            .getTopResults());
    }
}
