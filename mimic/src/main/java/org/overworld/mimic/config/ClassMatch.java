package org.overworld.mimic.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a ClassMatch directive from the application config.
 * <p/>
 * This directive specifies how to match classes for instrumentation, and
 * contains the MethodMatch instances that specify how to match the methods
 * therein
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2015-09-26
 */
public class ClassMatch {

    private String classPattern;
    private final List<MethodMatch> methodMatches = new ArrayList<MethodMatch>();
    private boolean regex;

    /**
     * @return the pattern to match against the class name
     */
    protected String getClassPattern() {

        return this.classPattern;
    }

    /**
     * @return the method matches from this instance
     */
    public List<MethodMatch> getMethodMatches() {

        return this.methodMatches;
    }

    /**
     * @return true if this class matcher is to match its class name by regex,
     *         false if the match is literal
     */
    protected boolean getRegex() {

        return this.regex;
    }

    /**
     * @param className
     *            the class name upon which to perform matching
     * @return true if the specified string matches this instance
     */
    public boolean matches(final String className) {

        return className.matches(this.classPattern);
    }

    /**
     * @param classPattern
     *            the pattern that this instance seeks to match
     * @return this instance for chaining
     */
    public ClassMatch setClassPattern(final String classPattern) {

        this.classPattern = classPattern;
        return this;
    }

    /**
     * @param methodMatches
     *            the method matches to set in this instance
     * @return this instance for chaining
     */
    public ClassMatch setMethodMatches(final List<MethodMatch> methodMatches) {

        this.methodMatches.addAll(methodMatches);
        return this;
    }

    /**
     * @param methodMatches
     *            a method match to set in this instance
     * @return this instance for chaining
     */
    public ClassMatch setMethodMatches(final MethodMatch methodMatch) {

        this.methodMatches.add(methodMatch);
        return this;
    }

    /**
     * @param true if this class matcher is to match its class name by regex,
     *        false if the match is literal
     * @return this instance for chaining
     */
    public ClassMatch setRegex(final boolean regex) {

        this.regex = regex;
        return this;
    }

    @Override
    public String toString() {

        return "ClassMatch [classPattern=" + this.classPattern
            + ", methodMatches=" + this.methodMatches + "]";
    }
}
