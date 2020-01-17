package org.overworld.mimic.cuesheet;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A serialisable class that fully describes a method call event for the
 * purposes of the agent
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2015-09-26
 */
public class MethodCall implements Serializable {

    private static final long serialVersionUID = -2425956192225899141L;

    /**
     * Constructs a key by which a MethodCall can be sorted and retrieved from a
     * HashMap.
     * <p/>
     * The key is based on the class and method static data and the arguments
     * passed only, thus the key does not change after the return value or
     * opcode is later added to the instance.
     * <p/>
     * This is provided as a static method so that keys can be constructed
     * without an existing instance for retrieval of instances from a Collection
     *
     * @param className
     *            the name of the class
     * @param methodName
     *            the name of the method
     * @param methodDesc
     *            the description of the method
     * @param arguments
     *            the arguments passed to the method in this call
     * @return a hashcode key based on static data and arguments passed only
     */
    public static int getKey(final String className, final String methodName,
        final String methodDesc, final Object[] arguments) {

        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((className == null) ? 0 : className.hashCode());
        result = prime * result
            + ((methodName == null) ? 0 : methodName.hashCode());
        result = prime * result
            + ((methodDesc == null) ? 0 : methodDesc.hashCode());
        result = prime * result + Arrays.hashCode(arguments);
        return result;
    }

    private final Object[] arguments;
    private final String className;
    private long duration;
    private final long invoked;
    private final String methodDesc;
    private final String methodName;
    private int opcode;
    private Object retVal;
    private final long threadId;

    /**
     * Constructs a new instance populated with static data about the method,
     * the thread id and the arguments passed
     *
     * @param threadId
     *            the tread id of the thread in which the method was called
     * @param className
     *            the name of the class
     * @param methodName
     *            the name of the method
     * @param methodDesc
     *            the description of the method
     * @param arguments
     *            the arguments passed in this method call
     */
    public MethodCall(final long threadId, final String className,
        final String methodName, final String methodDesc,
        final Object[] arguments) {

        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.arguments = arguments;
        this.threadId = threadId;

        this.invoked = System.currentTimeMillis();

        this.duration = -1;
        this.opcode = -1;
        this.retVal = null;
    }

    /**
     * Update this instance to add the value returned from the method, and the
     * opcode of the return
     *
     * @param opcode
     *            the opcode of the return type used in the bytecode
     * @param retVal
     *            the return value of the method, autoboxed if primitive
     */
    public void done(final int opcode, final Object retVal) {

        this.opcode = opcode;
        this.retVal = retVal;
        this.duration = System.currentTimeMillis() - this.invoked;
    }

    @Override
    public boolean equals(final Object obj) {

        if (this == obj) return true;
        if (obj == null) return false;
        if (this.getClass() != obj.getClass()) return false;
        final MethodCall other = (MethodCall) obj;
        if (!Arrays.deepEquals(this.arguments, other.arguments)) return false;
        if (this.className == null) {
            if (other.className != null) return false;
        } else if (!this.className.equals(other.className)) return false;
        if (this.duration != other.duration) return false;
        if (this.invoked != other.invoked) return false;
        if (this.methodDesc == null) {
            if (other.methodDesc != null) return false;
        } else if (!this.methodDesc.equals(other.methodDesc)) return false;
        if (this.methodName == null) {
            if (other.methodName != null) return false;
        } else if (!this.methodName.equals(other.methodName)) return false;
        if (this.opcode != other.opcode) return false;
        if (this.retVal == null) {
            if (other.retVal != null) return false;
        } else if (!this.retVal.equals(other.retVal)) return false;
        if (this.threadId != other.threadId) return false;
        return true;
    }

    /**
     * @return the arguments from this instance
     */
    public Object[] getArguments() {

        return this.arguments;
    }

    /**
     * @return the name of the class
     */
    public String getClassName() {

        return this.className;
    }

    /**
     * @return the duration of the observed call in milliseconds
     */
    public long getDuration() {

        return this.duration;
    }

    /**
     * @return the system time of the method invocation
     */
    public long getInvoked() {

        return this.invoked;
    }

    /**
     * @return the value of {@link MethodCall#getKey()} as applied to this
     *         instance
     */
    public int getKey() {

        /*
         * key is based on hashcodes. If arguments were known to have reliable
         * toString()s there'd be a good case for using Arrays.deepToString()
         * instead
         */

        return MethodCall.getKey(this.className, this.methodName,
            this.methodDesc, this.arguments);
    }

    /**
     * @return the method description from this instance
     */
    public String getMethodDesc() {

        return this.methodDesc;
    }

    /**
     * @return the method name from this instance
     */
    public String getMethodName() {

        return this.methodName;
    }

    /**
     * @return the opcode of the return uesd in the bytecode of the method as
     *         recorded in this instance
     */
    public int getOpcode() {

        return this.opcode;
    }

    /**
     * @return the returned value as recorded in this instance, autoboxed as
     *         necessary
     */
    public Object getRetVal() {

        return this.retVal;
    }

    /**
     * @return the id of the thread in which this method was called as recorded
     *         in this instance
     */
    public long getThreadId() {

        return this.threadId;
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(this.arguments);
        result = prime * result
            + ((this.className == null) ? 0 : this.className.hashCode());
        result = prime * result + (int) (this.duration ^ (this.duration >>> 32));
        result = prime * result + (int) (this.invoked ^ (this.invoked >>> 32));
        result = prime * result
            + ((this.methodDesc == null) ? 0 : this.methodDesc.hashCode());
        result = prime * result
            + ((this.methodName == null) ? 0 : this.methodName.hashCode());
        result = prime * result + this.opcode;
        result = prime * result + ((this.retVal == null) ? 0 : this.retVal.hashCode());
        result = prime * result + (int) (this.threadId ^ (this.threadId >>> 32));
        return result;
    }

    @Override
    public String toString() {

        return "MethodCall [arguments=" + Arrays.toString(this.arguments)
            + ", className=" + this.className + ", duration=" + this.duration
            + ", invoked=" + this.invoked + ", methodDesc=" + this.methodDesc
            + ", methodName=" + this.methodName + ", opcode=" + this.opcode
            + ", retVal=" + this.retVal + ", threadId=" + this.threadId + "]";
    }
}
