package org.overworld.mimic.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.overworld.mimic.AgentMechanism;

/**
 * Represents the MimicConfig directive from the Application Configuration
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2015-09-26
 */
public class MimicConfig {

    private String basename;

    private List<ClassMatch> classMatches = new ArrayList<ClassMatch>();

    private AgentMechanism mechanism;

    /**
     * @return the basename used for constructing filenames
     */
    public String getBasename() {

        return this.basename;
    }

    /**
     * @return the list of ClassMatches
     */
    public List<ClassMatch> getClassMatches() {

        return this.classMatches;
    }

    /**
     * @return the mechanism in use, reconciled or functional
     */
    public AgentMechanism getMechanism() {

        return this.mechanism;
    }

    /**
     * @param basename
     *            the basename to use for constructing filenames
     * @return this instance for chaining
     */
    public MimicConfig setBasename(final String basename) {

        this.basename = basename;
        return this;
    }

    /**
     * @param classMatch
     *            a ClassMatch to add to this instance
     * @return this instance for chaining
     */
    public MimicConfig setClassMatches(final ClassMatch classMatch) {

        this.classMatches.add(classMatch);
        return this;
    }

    /**
     * @param classMatches
     *            the ClassMatches to add to this instance
     * @return this instance for chaining
     */
    public MimicConfig setClassMatches(final Collection<ClassMatch> classMatches) {

        this.classMatches.addAll(classMatches);
        return this;
    }

    /**
     * @param classMatches
     *            a list of ClassMatches to set in this instance
     */
    public void setClassMatches(final List<ClassMatch> classMatches) {

        this.classMatches = classMatches;
    }

    /**
     * @param mechanism
     *            the mechanism in use, reconciled or functional
     */
    public void setMechanism(final AgentMechanism mechanism) {

        this.mechanism = mechanism;
    }

    @Override
    public String toString() {

        return "MimicConfig [basename=" + this.basename + ", classMatches="
            + this.classMatches + ", mechanism=" + this.mechanism + "]";
    }
}
