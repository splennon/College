package org.overworld.mimic;

/**
 * Describes mechanism of operation. Reconciled remembers calls of methods in
 * order based on thread, Functional assumes instrumented methods to be
 * stateless, returning the same output for any given input.
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-03-30
 */
public enum AgentMechanism {
    FUNCTIONAL, RECONCILED
}