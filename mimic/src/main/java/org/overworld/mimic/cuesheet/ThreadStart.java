package org.overworld.mimic.cuesheet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A serialisable class to record the identity of a thread based on the stack
 * trace of the execution point of the Thread.start() call that starts the
 * thread
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2015-09-26
 */
public class ThreadStart implements Serializable, Comparable<ThreadStart> {

    private final static Map<Integer, Integer> dedupes = new HashMap<Integer, Integer>();

    private static final long serialVersionUID = -5676480483977951854L;

    private final int sequence;
    private final StackTraceElement[] stack;
    private final String startingClass;
    private final String startingFile;
    private final int startingLine;
    private final String startingMethod;
    private final int threadPersistId;
    private final long threadTransientId;

    /**
     * Constructs a new instance that records the thread id and stack trace
     *
     * @param threadTransientId
     *            the thread id obtained from {@link Thread#getId()}
     * @param stackTrace
     *            the stack trace of the start point of the thread
     * @throws IllegalArgumentException
     *             if the stack trace has fewer than 4 elements
     */
    public ThreadStart(final Long threadTransientId,
        final StackTraceElement[] stackTrace) throws IllegalArgumentException {

        if (stackTrace.length < 4)
            throw new IllegalArgumentException("Stack trace too short"
                + Arrays.toString(stackTrace));

        this.stack = new StackTraceElement[stackTrace.length - 3];

        System.arraycopy(stackTrace, 3, this.stack, 0, this.stack.length);

        /* for convenience, copy out the details of the call site */

        final StackTraceElement top = this.stack[0];

        this.startingClass = top.getClassName();
        this.startingFile = top.getFileName();
        this.startingLine = top.getLineNumber();
        this.startingMethod = top.getMethodName();
        this.threadTransientId = threadTransientId;

        if (this.startingLine < 0)
            System.err.println("Source file line numbers not available, thread"
                + " identification may be compromised");

        /*
         * and make the checksum explicit and separate from Object.hashcode() to
         * facilitate inspection of the class in its externalised form(s)
         */

        this.threadPersistId = Arrays.deepHashCode(this.stack);

        synchronized (ThreadStart.dedupes) {

            if (!ThreadStart.dedupes.containsKey(this.threadPersistId))
                ThreadStart.dedupes.put(this.threadPersistId, 0);

            this.sequence = ThreadStart.dedupes.get(this.threadPersistId);
            ThreadStart.dedupes.put(this.threadPersistId, this.sequence + 1);
        }
    }

    /**
     * Compares this instance to another ThreadStart instance
     *
     * @param the
     *            other instance to which to compare
     * @return the comparison result comparable with
     *         {@link Comparable#compareTo(Object)}
     */
    @Override
    public int compareTo(final ThreadStart other) {

        if (this.getThreadPersistId() == other.getThreadPersistId()) {
            return Integer.compare(this.sequence, other.sequence);
        } else {
            return Integer.compare(this.threadPersistId, other.threadPersistId);
        }
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        final ThreadStart other = (ThreadStart) obj;
        if (this.sequence != other.sequence) return false;
        if (!Arrays.equals(this.stack, other.stack)) return false;
        if (this.startingClass == null) {
            if (other.startingClass != null) return false;
        } else if (!this.startingClass.equals(other.startingClass)) return false;
        if (this.startingFile == null) {
            if (other.startingFile != null) return false;
        } else if (!this.startingFile.equals(other.startingFile)) return false;
        if (this.startingLine != other.startingLine) return false;
        if (this.startingMethod == null) {
            if (other.startingMethod != null) return false;
        } else if (!this.startingMethod.equals(other.startingMethod)) return false;
        if (this.threadPersistId != other.threadPersistId) return false;
        if (this.threadTransientId != other.threadTransientId) return false;
        return true;
    }

    /**
     * @return the sequence in which this thread was started relateive to other
     *         threads that have the stame start point stack trace
     */
    public int getSequence() {

        return this.sequence;
    }

    /**
     * @return the stack trace of this instance
     */
    public StackTraceElement[] getStack() {

        return this.stack;
    }

    /**
     * @return the containing class that started this thread
     */
    public String getStartingClass() {

        return this.startingClass;
    }

    /**
     * @return the name of the file in which this thread was started
     */
    public String getStartingFile() {

        return this.startingFile;
    }

    /**
     * @return the number of the line on which this thread was started
     */
    public int getStartingLine() {

        return this.startingLine;
    }

    /**
     * @return the name of the method in which this thread was started
     */
    public String getStartingMethod() {

        return this.startingMethod;
    }

    /**
     * @return the persistent id generated from this thread based on its stack
     *         trace
     */
    public int getThreadPersistId() {

        return this.threadPersistId;
    }

    /**
     * @return the thread id of this thread returned by {@link Thread#getId()}
     *         at the time of record
     */
    public long getThreadTransientId() {

        return this.threadTransientId;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + this.sequence;
        result = prime * result + Arrays.hashCode(this.stack);
        result = prime * result
            + ((this.startingClass == null) ? 0 : this.startingClass.hashCode());
        result = prime * result
            + ((this.startingFile == null) ? 0 : this.startingFile.hashCode());
        result = prime * result + this.startingLine;
        result = prime * result
            + ((this.startingMethod == null) ? 0 : this.startingMethod.hashCode());
        result = prime * result + this.threadPersistId;
        result = prime * result
            + (int) (this.threadTransientId ^ (this.threadTransientId >>> 32));
        return result;
    }

    @Override
    public String toString() {

        return "ThreadStart [threadTransientId=" + this.threadTransientId
            + ", sequence=" + this.sequence + ", threadPersistId="
            + this.threadPersistId + ", startingClass=" + this.startingClass
            + ", startingFile=" + this.startingFile + ", startingLine="
            + this.startingLine + ", startingMethod=" + this.startingMethod
            + ", stack=" + Arrays.toString(this.stack) + "]";
    }
}
