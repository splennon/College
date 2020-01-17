package org.overworld.mimic.config;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.thoughtworks.xstream.XStream;

/**
 * Represents the application configuration structure read from the application
 * config file
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2015-09-26
 */
public class Config {

    /**
     * Produces a simple template configuration file which is useful instead of
     * writing configuration from scratch
     *
     * @param args
     *            unused
     */
    public static void main(final String[] args) {

        final MimicConfig config = new MimicConfig();

        final MethodMatch mm1 = (new MethodMatch()).setMethodPattern("<init>")
            .setSignatureMatches(".*").setSignatureEquals("()V");

        final MethodMatch mm2 = new MethodMatch().setMethodPattern("format")
            .setSignatureEquals(
                "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String");

        final ClassMatch cm1 = (new ClassMatch()).setClassPattern(
            "java/lang/AssertionError").setMethodMatches(mm1);

        final ClassMatch cm2 = (new ClassMatch()).setClassPattern(
            "java/lang/String").setMethodMatches(mm2);

        config.setBasename("MimicSampleConfig").setClassMatches(cm1)
        .setClassMatches(cm2);

        final Config sampleGenerator = new Config("MimicSampleConfig.xml");

        try {

            sampleGenerator.tofile(config);
        } catch (final IOException e) {

            System.err.println("Error generating sample configuration file");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private final String filename;
    private final XStream xstream;

    /**
     * Constructs a new instance to operate on the file named
     *
     * @param filename
     *            the name of the file to read or write
     */
    public Config(final String filename) {

        this.filename = filename;
        this.xstream = new XStream();

        this.xstream.alias("mimicConfig", MimicConfig.class);
        this.xstream.alias("classMatch", ClassMatch.class);
        this.xstream.alias("methodMatch", MethodMatch.class);

        this.xstream.addImplicitCollection(MimicConfig.class, "classMatches",
            ClassMatch.class);

        this.xstream.addImplicitCollection(ClassMatch.class, "methodMatches",
            MethodMatch.class);

        this.xstream.addImplicitCollection(MethodMatch.class,
            "signatureMatches", "signatureMatch", String.class);

        this.xstream.addImplicitCollection(MethodMatch.class,
            "signatureEquals", "signatureEqual", String.class);
    }

    /**
     * Extracts a complete MimicConfig from a named XML file
     *
     * @return the MimeConfig that was stored in the file
     * @throws IOException
     *             if the file cannot be found, opened or read
     */
    public MimicConfig fromFile()
        throws IOException {

        final Reader fileReader = new FileReader(this.filename);

        try {

            return (MimicConfig) this.xstream.fromXML(fileReader);
        } catch (final ClassCastException e) {

            throw new IOException("File does not contain a MimicConfig in a"
                + " format that can be read", e);
        } finally {

            fileReader.close();
        }
    }

    /**
     * Writes the given MimicConfig to the file associated with this instance
     *
     * @param config
     *            the configuration to write to file
     * @throws IOException
     *             if the file cannot be found, opened or read
     */
    public void tofile(final MimicConfig config)
        throws IOException {

        final Writer fileWriter = new FileWriter(this.filename);

        this.xstream.toXML(config, fileWriter);

        fileWriter.close();
    }

    @Override
    public String toString() {

        return "Config [filename=" + this.filename + ", xstream="
            + this.xstream + "]";
    }
}
