package org.overworld.mimic;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.overworld.mimic.cuesheet.MethodCall;
import org.overworld.mimic.cuesheet.ThreadStart;
import org.overworld.mimic.cuesheet.ThreadingSummary;

/**
 * An accounting class for the replay phase of the invocation of the agent.
 * <p/>
 * The methods herein are invoked by the instrumented methods of the target
 * codebase to effect replay of the nominated methods
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2015-09-26
 */
public class Replay {

    private static Map<Integer, MethodCall> calls = new HashMap<Integer, MethodCall>();
    private static Map<Long, Deque<MethodCall>> inFlightMethods = new ConcurrentHashMap<Long, Deque<MethodCall>>();
    private static AgentMechanism mechanism = AgentMechanism.FUNCTIONAL;
    private static Map<Long, ObjectInputStream> methodCues = new ConcurrentHashMap<Long, ObjectInputStream>();
    private static String prefix;
    private static ThreadingSummary threadingSummary = new ThreadingSummary();
    private static Map<Long, Long> threadReconcilliation = new HashMap<Long, Long>();

    /**
     * Called on entry to an instrumented method during the replay phase
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

        if (Replay.reentered()) return;

        final long threadId = Thread.currentThread().getId();

        MethodCall mc = null;

        if (mechanism == AgentMechanism.RECONCILED) {

            final ObjectInputStream ois = Replay.methodCues.get(threadId);

            try {

                mc = (MethodCall) ois.readObject();
            } catch (final Exception e) {

                System.err.println("Unable to read from method cuesheet file");
                e.printStackTrace();
                System.exit(1);
            }
        } else {

            mc = calls.get(MethodCall.getKey(className, methodName, methodDesc,
                arguments));
        }

        Replay.inFlightMethods.get(threadId).push(mc);

        /* simulate the original delay of the method execution */

        final long targetWakeTime = System.currentTimeMillis()
            + mc.getDuration();

        while (System.currentTimeMillis() < targetWakeTime) {

            try {

                Thread.sleep(targetWakeTime - System.currentTimeMillis());
            } catch (final InterruptedException e) {

                /* not interested in InterruptedException, loop will repeat */
            }
        }
    }

    /**
     * Called on exit from an instrumented method during replay phase
     */
    public static int exit() {

        if (Replay.reentered())
            throw new MimicException("Detected reentrance of Replay call");

        final long threadId = Thread.currentThread().getId();

        final MethodCall mc = Replay.inFlightMethods.get(threadId).pop();

        return mc.getOpcode();
    }

    /**
     * @return the cached return value for replay
     */
    public static Object getRetVal() {

        if (Replay.reentered())
            throw new MimicException("Detected reentrance of Replay call");

        final long threadId = Thread.currentThread().getId();

        /* this is not the exiting call, so merely peek at the MethodCall */

        final MethodCall mc = Replay.inFlightMethods.get(threadId).peekFirst();

        return mc.getRetVal();
    }

    /**
     * Initialise and set the prefix by which files are named and open any
     * initial ObjectInputStreams
     * <p/>
     * This must run in the current thread before any other threads are spawned
     *
     * @param prefix
     *            the prefix for input files
     * @param agentMechanism
     */
    public static void initialise(final String prefix,
        final AgentMechanism agentMechanism) {

        Replay.prefix = prefix;

        Replay.mechanism = agentMechanism;

        /* get the original thread summary made during record */

        if (mechanism == AgentMechanism.RECONCILED) {

            try {

                final ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(prefix + ".bin"));
                Replay.threadingSummary = (ThreadingSummary) ois
                    .readObject();
                ois.close();
            } catch (final Exception e) {

                System.err.println("Cannot read threading schedule " + prefix
                    + ".bin");
                e.printStackTrace();
                System.exit(1);
            }
        } else {

            try {

                final ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(prefix + "_cache.bin"));

                @SuppressWarnings("unchecked")
                final Map<Integer, MethodCall> readObject = (Map<Integer, MethodCall>) ois
                .readObject();
                Replay.calls = readObject;

                ois.close();
            } catch (final Exception e) {

                System.err.println("Cannot read method call cache " + prefix
                    + "_cache.bin");
                e.printStackTrace();
                System.exit(1);
            }
        }

        /*
         * add the main thread to the reconciliation map
         */

        final long mainThreadId = Thread.currentThread().getId();

        Replay.threadReconcilliation.put(mainThreadId,
            Replay.threadingSummary.getMainThreadId());

        Replay.inFlightMethods.put(mainThreadId, new LinkedList<MethodCall>());

        ObjectInputStream ois = null;

        if (mechanism == AgentMechanism.RECONCILED) {

            try {

                ois = new ObjectInputStream(new FileInputStream(
                    Replay.prefix + "-"
                        + Replay.threadingSummary.getMainThreadId()
                        + ".bin"));
            } catch (final IOException e) {

                System.err.println("Cannot open file for root thread input"
                    + " cuesheet");
                e.printStackTrace();
                System.exit(1);
                return; /* to satisfy the compiler that ois must be initialised */
            }

            Replay.methodCues.put(mainThreadId, ois);
        }
    }

    /**
     * Detect reentrance, where an invocation of any Replay method results in
     * the ultimate calling of another Replay method before return of the first.
     * The first three stack frames are exempt, as they will be
     * Thread.getStackTrace(), the calling Replay method, and Replay.reentered()
     * itself, but a Replay method must not appear thereafter in the stack
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
             * Replay class before then return true
             */

            if (Replay.class.getName().equals(stea[i].getClassName()))
                return true;
        }
        return false;
    }

    /**
     * Record that a thread has been start()ed in the system. The thread is
     * assigned a persistent ID that is designed to be valid across runs of the
     * application, which is recorded in the threads map and also persisted to
     * disk as part of the cuesheet when using the reconciled mechanism.
     * <p/>
     * This method should be called from the start() method of the thread,
     * injected by instrumentation. It relies on the Thread.getStackTrace() of
     * the current thread and therefore cannot be instrumented into the run() or
     * any other method.
     *
     * @param threadId
     */
    public static void threadStart(final long threadId) {

        Replay.inFlightMethods.put(threadId, new LinkedList<MethodCall>());

        if (Replay.mechanism == AgentMechanism.RECONCILED) {

            final int persistId = (new ThreadStart(threadId, Thread.currentThread()
                .getStackTrace())).getThreadPersistId();

            long oldThreadId = -1;

            for (final ThreadStart ts : Replay.threadingSummary) {

                if (ts.getThreadPersistId() == persistId) {

                    /*
                     * threadReconcilliation maps current thread IDs to what they
                     * were in the Record phase
                     */

                    oldThreadId = ts.getThreadTransientId();
                    Replay.threadReconcilliation.put(threadId, oldThreadId);
                    Replay.threadingSummary.remove(ts);
                    break;
                }
            }

            if (oldThreadId < 0)
                throw new MimicException("Thread cannot be reconilled: " + threadId);

            try {

                Replay.methodCues.put(threadId, new ObjectInputStream(
                    new FileInputStream(Replay.prefix + "-" + oldThreadId
                        + ".bin")));
            } catch (final Exception e) {

                System.err.println("Unable to read method cuesheet file");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }
}
