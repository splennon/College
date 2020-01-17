package org.overworld.mimic;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.overworld.mimic.cuesheet.MethodCall;
import org.overworld.mimic.cuesheet.ThreadStart;
import org.overworld.mimic.cuesheet.ThreadingSummary;

/**
 * An accounting class for the record phase of the invocation of the agent.
 * <p/>
 * These methods are invoked by the instrumented methods of the target
 * codebase to effect recording of the nominated methods
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2015-09-26
 */
public class Record {

    private static Map<Integer, MethodCall> calls = new HashMap<Integer, MethodCall>();
    private static Map<Long, Deque<MethodCall>> inFlightMethods = new ConcurrentHashMap<Long, Deque<MethodCall>>();
    private static AgentMechanism mechanism = AgentMechanism.FUNCTIONAL;
    private static Map<Long, ObjectOutputStream> methodCues = new ConcurrentHashMap<Long, ObjectOutputStream>();
    private static String prefix;
    private static ThreadingSummary threadingSummary = new ThreadingSummary();
    private static Map<Long, Integer> threads = new ConcurrentHashMap<Long, Integer>();

    /**
     * Called on entry to an instrumented method during the record phase
     *
     * @param className
     *            the calling class name
     * @param methodName
     *            the calling method name
     * @param methodDesc
     *            the description of the calling method
     * @param arguments
     *            the arguments of the calling method
     */
    public static void entry(final String className, final String methodName,
        final String methodDesc, final Object[] arguments) {

        if (Record.reentered()) return;

        final long threadId = Thread.currentThread().getId();

        final MethodCall mc = new MethodCall(threadId, className, methodName,
            methodDesc, arguments);

        Record.inFlightMethods.get(threadId).push(mc);
    }

    /**
     * Called on exit from an instrumented method during record phase
     *
     * @param retVal
     *            the return value of the instrumented method
     * @param opcode
     *            the return opcode (Type of return) of the instrumented method
     */
    public static void exit(final Object retVal, final int opcode) {

        if (Record.reentered()) return;

        final long threadId = Thread.currentThread().getId();

        final MethodCall mc = Record.inFlightMethods.get(threadId).pop();

        mc.done(opcode, retVal);

        /* based on mechanism */

        if (mechanism == AgentMechanism.RECONCILED) {

            try {

                final ObjectOutputStream oos = Record.methodCues
                    .get(threadId);

                oos.writeObject(mc);
                oos.flush();
            } catch (final Exception e) {

                System.err.println("Unable to write to method cuesheet file");
                e.printStackTrace();
                System.exit(1);
            }

        } else {

            calls.put(mc.getKey(), mc);
        }
    }

    /**
     * Initialise and set the prefix by which files are named and open any
     * initial ObjectOutputStreams
     * <p/>
     * This must run in the current thread before any other threads are spawned
     *
     * @param prefix
     *            the prefix for output files
     * @param agentMechanism
     */
    public static void initialise(final String prefix, final AgentMechanism agentMechanism) {

        Record.prefix = prefix;

        Record.mechanism = agentMechanism;

        /*
         * arrange for flush and close of all output streams under most
         * circumstances upon JVM exit. The exact data recorded and ordering of
         * streams varies somewhat depending on the record mechanism: reconciled
         * or functional
         */
        if (mechanism == AgentMechanism.RECONCILED) {
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {

                    for (final ObjectOutputStream oos : Record.methodCues
                        .values()) {

                        try {

                            oos.flush();
                            oos.close();
                        } catch (final Exception e) {

                            /*
                             * any exception will be ignored here as the java
                             * runtime is exiting and there isn't a lot we can
                             * do if the file won't close
                             */
                            e.printStackTrace();
                        }
                    }

                    /* create the ObjectOutputStream for Thread Create */
                    try {

                        final ObjectOutputStream oos = new ObjectOutputStream(
                            new FileOutputStream(prefix
                                + ".bin"));

                        oos.writeObject(Record.threadingSummary);
                        oos.flush();
                        oos.close();
                    } catch (final IOException e) {

                        System.err.println("Cannot write threading summary");
                        e.printStackTrace();
                    }
                }
            });
        } else {

            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {

                    /* create the ObjectOutputStream for method calls cache */
                    try {

                        final ObjectOutputStream oos = new ObjectOutputStream(
                            new FileOutputStream(prefix + "_cache.bin"));

                        oos.writeObject(Record.calls);
                        oos.flush();
                        oos.close();
                    } catch (final IOException e) {

                        System.err.println("Cannot write method calls cache");
                        e.printStackTrace();
                    }
                }
            });
        }

        /*
         * add the main thread to the threadCues, as it predates instrumentation
         */

        final long mainThreadId = Thread.currentThread().getId();

        Record.threadingSummary.setMainThreadId(mainThreadId);

        Record.inFlightMethods.put(mainThreadId, new LinkedList<MethodCall>());

        /*
         * a 0 checksum would result from an empty stack trace, so it should be
         * safe to use 0 as the persistentThreadId of main, as all others will
         * have a stack trace from their call site
         */
        Record.threads.put(mainThreadId, 0);

        ObjectOutputStream oos = null;

        if (mechanism == AgentMechanism.RECONCILED) {

            try {

                oos = new ObjectOutputStream(new FileOutputStream(
                    Record.prefix + "-" + mainThreadId + ".bin"));
            } catch (final IOException e) {

                System.err.println("Cannot open file for root thread output"
                    + " cuesheet");
                e.printStackTrace();
                System.exit(1);
                return; /* to satisfy the compiler that oos must be initialised */
            }

            Record.methodCues.put(mainThreadId, oos);
        }
    }

    /**
     * Detect reentrance, where an invocation of any Record method results in
     * the ultimate calling of another Record method before return of the first.
     * The first three stack frames are exempt, as they will be
     * Thread.getStackTrace(), the calling Record method, and Record.reentered()
     * itself, but a Record method must not appear thereafter in the stack
     * trace, should it do so reentrace is detected
     *
     * @return true if reentrance is detected, false otherwise
     */
    private static boolean reentered() {

        final StackTraceElement[] stea = Thread.currentThread().getStackTrace();

        assert stea.length >= 4;

        for (int i = 3; i < stea.length; i++) {

            /*
             * for every stack trace element, if we have been in a method of the
             * Record class before then return true
             */

            if (Record.class.getName().equals(stea[i].getClassName()))
                return true;
        }
        return false;
    }

    /**
     * Record that a thread has been start()ed in the system. The thread is
     * assigned a persistent ID that is designed to be valid across runs of the
     * application, which is recorded in the threads map and also (With
     * reconciled mechanism) persisted to disk as part of the cuesheet.
     * <p/>
     * This method should be called from the start() method of the thread,
     * injected by instrumentation. It relies on the Thread.getStackTrace() of
     * the current thread and therefore cannot be instrumented into the run() or
     * any other method.
     *
     * @param threadId
     */
    public static void threadStart(final long threadId) {

        Record.inFlightMethods.put(threadId, new LinkedList<MethodCall>());

        if (Record.mechanism == AgentMechanism.RECONCILED) {
            try {

                Record.methodCues.put(threadId, new ObjectOutputStream(
                    new FileOutputStream(Record.prefix + "-" + threadId + ".bin")));
            } catch (final Exception e) {

                System.err.println("Unable open method cuesheet file");
                e.printStackTrace();
                System.exit(1);
            }

            final ThreadStart ts = new ThreadStart(threadId, Thread.currentThread()
                .getStackTrace());

            Record.threads.put(threadId, ts.getThreadPersistId());

            Record.threadingSummary.add(ts);
        }
    }
}
