package org.overworld.metre;

/**
 * Thrown to indicate an exception from which the application cannot recover.
 * <p/>
 * The application is in an inconsistent or irrecoverable state and should be
 * exited. It is for this reason that this is an unchecked exception.
 * <p/>
 * Instances of this exception will often be caught by the Application's
 * Uncaught Exception Handler.
 * <p/>
 * An <code>ApplicationException</code> can optionally contain a <code>String
 * </code> detail message and a <code>Throwable</code> cause which was the cause
 * of this exception to facilitate exception wrapping.
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class ApplicationException extends Exception {

    private static final long serialVersionUID = 83295634L;

    /**
     * Constructs a new <code>ApplicationException</code> without a detail
     * message or causal Throwable.
     */
    public ApplicationException() {

        super();
    }

    /**
     * Constructs a new <code>ApplicationException</code> with the specified
     * detail message.
     *
     * @param message
     *            the detail message
     */
    public ApplicationException(final String message) {

        super(message);
    }

    /**
     * Constructs a new <code>ApplicationException</code> specifying <code>
     * cause</code> as the Throwable that caused this exception and with the
     * specified detail message.
     *
     * @param cause the <code>Throwable</code> that caused this exception to be
     * thrown
     * @param message the detail message
     */
    public ApplicationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new <code>ApplicationException</code> specifying <code>
     * cause</code> as the Throwable that caused this exception.
     *
     * @param cause the <code>Throwable</code> that caused this exception to be
     * thrown
     */
    public ApplicationException(final Throwable cause) {
        super(cause);
    }
}
