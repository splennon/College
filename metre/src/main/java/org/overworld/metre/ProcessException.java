package org.overworld.metre;

/**
 * Thrown to indicate an error in handling OS processes
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class ProcessException extends ApplicationException {

    private static final long serialVersionUID = 54739783L;

    /**
     * Constructs a new <code>ProcessException</code> without a detail message
     * or causal Throwable.
     */
    public ProcessException() {

        super();
    }

    /**
     * Constructs a new <code>ProcessException</code> with the specified detail
     * message.
     *
     * @param message
     *            the detail message
     */
    public ProcessException(final String message) {

        super(message);
    }

    /**
     * Constructs a new <code>ProcessException</code> specifying <code>
     * cause</code> as the Throwable that caused this exception and with the
     * specified detail message.
     *
     * @param cause
     *            the <code>Throwable</code> that caused this exception to be
     *            thrown
     * @param message
     *            the detail message
     */
    public ProcessException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new <code>ProcessException</code> specifying <code>
     * cause</code> as the Throwable that caused this exception.
     *
     * @param cause
     *            the <code>Throwable</code> that caused this exception to be
     *            thrown
     */
    public ProcessException(final Throwable cause) {
        super(cause);
    }
}
