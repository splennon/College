package org.overworld.mimic;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.overworld.mimic.config.ClassMatch;
import org.overworld.mimic.config.Config;
import org.overworld.mimic.config.MethodMatch;
import org.overworld.mimic.config.MimicConfig;

/**
 * The entry class to the Mimic agent, contains the premain method to service
 * the -javaagent invocation
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2015-09-26
 */
public class MimicAgent {

    /**
     * Provides central handling of uncaught exceptions
     *
     * @param t
     *            the exception to handle
     */
    private static void handleException(final Throwable t) {
        if (t instanceof MimicException) {

            System.err.println("Installation of Mimic in the "
                + "current applicaiton has failed. Terminating "
                + "the JVM");
            t.printStackTrace();
            System.exit(1);
        } else {
            t.printStackTrace();
        }
    }

    /**
     * Install the agent in the appropriate Replay or Record mode, and install
     * all appropriate instrumentation.
     *
     * @param agentArgument
     *            the argument passed in the -javaagent argument, consisting of
     *            the direction to Record or Replay, followed by the application
     *            configuration xml file, comma delimited
     * @param instrumentation
     *            the instrument object provided by the JVM
     */
    private static void installAgent(final String agentArgument,
        final Instrumentation instrumentation) {

        /* parse the agent argument */

        String[] args;

        try {

            args = agentArgument.split(",");

            /* simple test for 2 or more pieces to the string */

            args[1] = args[1];

        } catch (final Exception e) {

            throw new MimicException(String.format("Unable to parse "
                + "argument to Mimic agent: \"%s\"", agentArgument), e);
        }

        /* get the agent mode of operation */

        AgentMode agentMode = null;

        try {

            agentMode = AgentMode.valueOf(args[0].toUpperCase());
        } catch (final Exception e) {

            throw new MimicException(String.format(
                "Agent mode %s is unknown, try record or replay", args[0]));
        }

        /* get the configuration file */

        MimicConfig applicationConfig;

        try {

            applicationConfig = (new Config(args[1])).fromFile();
        } catch (final IOException e) {

            throw new MimicException("Error accessing config file: " + args[1],
                e);
        }

        /*
         * Thread requires instrumentation with special handling, so it is
         * cleanest to add it to the application config implicitly
         */
        applicationConfig.setClassMatches(new ClassMatch().setClassPattern(
            "java.lang.Thread").setMethodMatches(
                new MethodMatch().setMethodPattern("start").setSignatureEquals(
                    "()V")));

        /* make mode and config final for use in inner class */

        final AgentMode mode = agentMode;
        final MimicConfig config = applicationConfig;

        switch (mode) {
        case RECORD:
            Record.initialise(config.getBasename(), config.getMechanism());
            break;
        case REPLAY:
            Replay.initialise(config.getBasename(), config.getMechanism());
            break;
        default:
            throw new AssertionError("Invalid code path");
        }

        /*
         * add the classfile transformer that is responsible for transforming
         * all classes that should be instrumented
         */

        instrumentation.addTransformer(new ClassFileTransformer() {

            @Override
            public byte[] transform(final ClassLoader classLoader,
                final String className, final Class<?> clazz,
                final ProtectionDomain protectionDomain, final byte[] bytes) {

                /*
                 * scan the application configuration to see if this should be
                 * instrumented
                 */

                List<MethodMatch> methodMatches = null;

                for (final ClassMatch cm : config.getClassMatches()) {

                    if (cm.matches(className)) {
                        methodMatches = cm.getMethodMatches();
                        break;
                    }
                }

                /*
                 * if there was no matching ClassMatch in the application
                 * configuration then it is not necessary to instrument this
                 * method
                 */

                if ((methodMatches == null || methodMatches.isEmpty()))
                    return null;

                try {

                    final ClassReader reader = new ClassReader(bytes);
                    final ClassWriter writer = new ClassWriter(reader,
                        ClassWriter.COMPUTE_MAXS + ClassWriter.COMPUTE_FRAMES);

                    final ClassVisitor visitor = new MimicClassVisitor(writer,
                        methodMatches, mode);

                    reader.accept(visitor, ClassReader.EXPAND_FRAMES);

                    final byte[] ba = writer.toByteArray();

                    // print the instrumented bytecode for manual checking
                    //
                    // final OutputStream bos = new FileOutputStream(className
                    // .replace('/', '_') + ".class");
                    // bos.write(ba);
                    // bos.close();

                    return ba;
                } catch (final Exception e) {

                    throw new MimicException("Exception instrumenting class", e);
                }
            }
        }, true); /*
                   * true allows retransformation using this transformer, this
                   * is very important as without it calls to retransformClass
                   * fail silently and use cached bytecode from class load time
                   * without transforms
                   */

        /*
         * reinstrument all classes in the application config that have already
         * been loaded, this may include some from java.lang.* which were loaded
         * into the bootstrap classloader from rt.jar
         */

        for (final Class<?> clazz : instrumentation.getAllLoadedClasses()) {

            for (final ClassMatch cm : applicationConfig.getClassMatches()) {

                if (cm.matches(Type.getInternalName(clazz))) {

                    try {

                        instrumentation.retransformClasses(clazz);
                    } catch (final UnmodifiableClassException e) {

                        throw new MimicException(String.format(
                            "Class is not modifiable: %s", clazz.getName()), e);
                    }
                }
            }
        }
    }

    /**
     * The premain method is called before application main to install the agent
     *
     * @param agentArgument
     *            the argument passed in the -javaagent argument, consisting of
     *            the direction to Record or Replay, followed by the application
     *            configuration xml file, comma delimited
     * @param instrumentation
     *            the instrument object provided by the JVM
     */
    public static void premain(final String agentArgument,
        final Instrumentation instrumentation) {

        /* install the default uncaught exception handler */

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(final Thread t, final Throwable e) {

                MimicAgent.handleException(e);
            }
        });

        try {

            MimicAgent.installAgent(agentArgument, instrumentation);
        } catch (final Throwable t) {

            MimicAgent.handleException(t);
        }
    }
}
