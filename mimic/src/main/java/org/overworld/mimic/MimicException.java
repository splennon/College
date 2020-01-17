package org.overworld.mimic;

import java.util.Arrays;

/**
 * High level exception indicating internal error in the operation of the Mimic
 * Agent
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2015-09-26
 */
public class MimicException extends RuntimeException {

    public static boolean DEBUG = true;

    private static final long serialVersionUID = -4926575691381570482L;

    /**
     * Constructs a new <code>MimicException</code> without a detail message or
     * causal Throwable.
     */
    public MimicException() {
        super();

        if (MimicException.DEBUG) System.err.println(this.toString());
    }

    /**
     * Constructs a new <code>MimicException</code> with the specified detail
     * message.
     *
     * @param message
     *            the detail message
     */
    public MimicException(final String message) {
        super(message);

        if (MimicException.DEBUG) System.err.println(this.toString());
    }

    /**
     * Constructs a new <code>MimicException</code> specifying <code>
     * cause</code> as the Throwable that caused this exception and with the
     * specified detail message.
     *
     * @param cause
     *            the <code>Throwable</code> that caused this exception to be
     *            thrown
     * @param message
     *            the detail message
     */
    public MimicException(final String message, final Throwable cause) {
        super(message, cause);

        if (MimicException.DEBUG) System.err.println(this.toString());
    }

    /**
     * Constructs a new <code>MimicException</code> specifying <code>
     * cause</code> as the Throwable that caused this exception.
     *
     * @param cause
     *            the <code>Throwable</code> that caused this exception to be
     *            thrown
     */
    public MimicException(final Throwable cause) {
        super(cause);

        if (MimicException.DEBUG) System.err.println(this.toString());
    }

    @Override
    public String toString() {

        return "MimicException [getMessage()=" + this.getMessage()
        + ", getCause()=" + this.getCause() + ", toString()="
        + super.toString() + ", getStackTrace()="
        + Arrays.toString(this.getStackTrace()) + ", getStackTraceDepth()="
        + "]";
    }
}