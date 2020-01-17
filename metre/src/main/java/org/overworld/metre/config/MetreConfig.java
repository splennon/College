package org.overworld.metre.config;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.overworld.metre.ApplicationException;

import com.thoughtworks.xstream.XStream;

/**
 * Reads and writes the application configuration file including the generation
 * of a basic sample file as a starting point.
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class MetreConfig {

    public static final String COPY_CMS = "-XX:+UseConcMarkSweepGC -XX:-UseParNewGC";
    public static final String COPY_MSCOMPACT = "-XX:+UseSerialGC";
    public static final String EXAMPLE_FILENAME = "Config_Example.xml";
    public static final String FILENAME = "Config.xml";
    public static final String G1GC = "-XX:+UseG1GC";
    public static final String PARNEW_CMS = "-XX:+UseConcMarkSweepGC";
    public static final String PARNEW_MSCOMPACT = "-XX:+UseParNewGC";
    public static final String PSSCAVENGE_PSMS = "-XX:+UseParallelGC -XX:+UseParallelOldGC";

    /**
     * Produces a simple template configuration file which is a useful starting
     * point for constructing application configuration instead of starting from
     * scratch
     *
     * @param args
     *            unused
     */
    public static void main(final String[] args) {

        final ConfigRoot config = new ConfigRoot();

        config
        .setJmeterOptions((new JMeter())
            .setInitialHeap("50")
            .setMaxHeap("2048")
            .setLogFile("./jmeter.log")
            .setJava(
                "/Library/Java/JavaVirtualMachines/jdk1.8.0_65.jdk/Contents/Home/bin/java")
            .setTestFile("jmeter.jmx"));

        config
        .setTargetOptions((new Target())
            .setArguments("")
            .setInvocation("./syno.jar")
            .setJvmParams(
                "-Dcom.sun.management.jmxremote.port=9690 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false")
            .setJava(
                "/Library/Java/JavaVirtualMachines/jdk1.8.0_65.jdk/Contents/Home/bin/java"));

        final List<StaticParameters> statics = new ArrayList<>();
        config.setStatics(statics);

        final BoolParameter useCMSInitiatingOccupancyOnly = new BoolParameter(
            "UseCMSInitiatingOccupancyOnly", false,
            "Start CMS collector based only on Occupancy value");

        final StaticParameters p1 = new StaticParameters(COPY_CMS,
            "Young Copy and Old ConcurrentMarkSweep");
        p1.getDynamics().add(useCMSInitiatingOccupancyOnly);
        statics.add(p1);

        statics.add(new StaticParameters(COPY_MSCOMPACT,
            "Young Copy and Old MarkSweepCompact"));

        statics.add(new StaticParameters(G1GC, "Young G1 and Old G1"));

        final StaticParameters p2 = new StaticParameters(PARNEW_CMS,
            "Young ParNew and Old ConcurrentMarkSweep");
        p2.getDynamics().add(useCMSInitiatingOccupancyOnly);
        statics.add(p2);

        statics.add(new StaticParameters(PARNEW_MSCOMPACT,
            "Young ParNew and Old MarkSweepCompact"));

        statics.add(new StaticParameters(PSSCAVENGE_PSMS,
            "Young PS Scavenge and Old PS MarkSweep"));

        final List<Parameter> dynamics = new ArrayList<>();

        dynamics
        .add(new IntegralStepParameter("MinHeapFreeRatio", 5, 50, 15,
            "The minimum allowed percentage of free space before heap size increase"));
        dynamics.add(new BoolParameter("UseAdaptiveSizePolicy", false,
            "Adaptive sizing of generations"));
        dynamics.add(new StringListParameter("TLABSize", new String[] { "0",
            "512k", "1m" }, "Size of the Thread Local allocation buffer"));
        config.setDynamics(dynamics);

        try {

            (new MetreConfig(EXAMPLE_FILENAME)).tofile(config);
        } catch (final IOException e) {

            System.err.println("Error generating sample configuration file");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private final XStream xstream;

    /**
     * Constructs a new instance to operate on EXAMPLE_FILENAME.
     */
    public MetreConfig() {

        this(EXAMPLE_FILENAME);
    }

    /**
     * Constructs a new instance to operate on the file named
     *
     * @param filename
     *            the name of the file to read or write
     */
    public MetreConfig(final String filename) {

        this.xstream = new XStream();

        this.xstream.alias("configRoot", ConfigRoot.class);
        this.xstream.alias("staticParameters", StaticParameters.class);
        this.xstream.alias("boolParameter", BoolParameter.class);
        this.xstream
        .alias("integralStepParameter", IntegralStepParameter.class);
        this.xstream.alias("stringListParameter", StringListParameter.class);
    }

    /**
     * Extract a complete ConfigRoot from the default XML file
     *
     * @return the ConfigRoot that was stored in the file
     * @throws ApplicationException
     *             if the file cannot be found, opened or read
     */
    public ConfigRoot fromFile() throws ApplicationException {

        try {

            return this.fromFile(FILENAME);
        } catch (final IOException e) {

            throw new ApplicationException(
                "Unable to read application configuration from file "
                    + FILENAME, e);
        }
    }

    /**
     * Extract a complete ConfigRoot from a named XML file
     *
     * @param filename
     *            the name of the file to read
     * @return the ConfigRoot that was stored in the file
     * @throws IOException
     *             if the file cannot be found, opened or read
     */
    public ConfigRoot fromFile(final String filename) throws IOException {

        final Reader fileReader = new FileReader(filename);

        try {

            return (ConfigRoot) this.xstream.fromXML(fileReader);
        } catch (final ClassCastException e) {

            throw new IOException("File does not contain a ConfigRoot in a"
                + " format that can be read", e);
        } finally {

            fileReader.close();
        }
    }

    /**
     * Write the given ConfigRoot to the file associated with this instance
     *
     * @param config
     *            the configuration to write to file
     * @throws IOException
     *             if the file cannot be found, opened or read
     */
    public void tofile(final ConfigRoot config) throws IOException {

        final Writer fileWriter = new FileWriter(EXAMPLE_FILENAME);

        this.xstream.toXML(config, fileWriter);

        fileWriter.close();
    }

    @Override
    public String toString() {

        return "MetreConfig [xstream=" + this.xstream + "]";
    }
}
