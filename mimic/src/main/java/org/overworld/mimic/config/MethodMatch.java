package org.overworld.mimic.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a MethodMatch directive from the application config.
 * <p/>
 * This directive specifies how to match methods within classes that have
 * matched a ClassMatch therein
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2015-09-26
 */
public class MethodMatch {

    private String methodPattern;
    private boolean regex;
    private List<String> signatureEquals = new ArrayList<String>();
    private List<String> signatureMatches = new ArrayList<String>();

    /**
     * @return the pattern to match against the class name
     */
    protected String getMethodPattern() {

        return this.methodPattern;
    }

    /**
     * @return true if this method matcher is to match its method name by regex,
     *         false if the match is literal
     */
    protected boolean getRegex() {

        return this.regex;
    }

    /**
     * @return the list of signatures that are to be matched literally when
     *         matching this method
     */
    protected List<String> getSignatureEquals() {

        /* XStream may have left this at null */

        if (this.signatureEquals == null)
            this.signatureEquals = new ArrayList<String>();

        return this.signatureEquals;
    }

    /**
     * @return the list of signatures that are to be matched as regexes when
     *         matching this method
     */
    protected List<String> getSignatureMatches() {

        /* XStream may have left this at null */

        if (this.signatureMatches == null)
            this.signatureMatches = new ArrayList<String>();

        return this.signatureMatches;
    }

    /**
     * @param methodName
     *            the method name upon which to match
     * @return true if the specified string matches this instance
     */
    public boolean matches(final String methodName) {

        return methodName.matches(this.methodPattern);
    }

    /**
     * @param signature
     *            the signature upon which to perform matching
     * @return true if the specified string matches this instance
     */
    public boolean matchesSignature(final String signature) {

        for (final String match : this.getSignatureEquals()) {
            if (signature.equals(match)) return true;
        }

        for (final String match : this.getSignatureMatches()) {
            if (signature.matches(match)) return true;
        }

        return false;
    }

    /**
     * @param methodPattern
     *            the pattern that this instance seeks to match
     * @return this instance for chaining
     */
    public MethodMatch setMethodPattern(final String methodPattern) {

        this.methodPattern = methodPattern;
        return this;
    }

    /**
     * @param true if this method matcher is to match its method name by regex,
     *        false if the match is literal
     * @return this instance for chaining
     */
    public MethodMatch setRegex(final boolean regex) {

        this.regex = regex;
        return this;
    }

    /**
     * @param signatureEquals
     *            the list of signatures for literal matching in this instance
     * @return this instance for chaining
     */
    public MethodMatch setSignatureEquals(final List<String> signatureEquals) {

        this.signatureEquals.addAll(signatureEquals);
        return this;
    }

    /**
     * @param signatureEquals
     *            a signatures to add to the list of signatures for literal
     *            matching in this instance
     * @return this instance for chaining
     */
    public MethodMatch setSignatureEquals(final String signatureEqual) {

        this.signatureEquals.add(signatureEqual);
        return this;
    }

    /**
     * @param signatureEquals
     *            the list of signatures for regex matching in this instance
     * @return this instance for chaining
     */
    public MethodMatch setSignatureMatches(final List<String> signatureMatches) {

        this.signatureMatches.addAll(signatureMatches);
        return this;
    }

    /**
     * @param signatureEquals
     *            a signatures to add to the list of signatures for regex
     *            matching in this instance
     * @return this instance for chaining
     */
    public MethodMatch setSignatureMatches(final String signatureMatch) {

        this.signatureMatches.add(signatureMatch);
        return this;
    }

    @Override
    public String toString() {

        return "MethodMatch [methodPattern=" + this.methodPattern + ", regex="
            + this.regex + ", signatureEquals=" + this.signatureEquals
            + ", signatureMatches=" + this.signatureMatches + "]";
    }
}
