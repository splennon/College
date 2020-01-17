package org.overworld.metre.config;

import java.util.ArrayList;
import java.util.List;

/**
 * A POJO to represent the values of the staticParameters sections in the
 * configuration file.
 * <p/>
 * Each staticParameters section contains both a list of static parameters that
 * do not expand, and optionally some dynamic parameters to expand and add to
 * the static parameters
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class StaticParameters extends Parameter {

    private List<Parameter> dynamics = new ArrayList<>();

    private String parameters;

    /**
     * @param parameters
     *            the static parameters
     * @param description
     *            a description of this staticParameters section
     */
    public StaticParameters(final String parameters, final String description) {

        super(description);

        this.parameters = parameters;
    }

    /**
     * Advance to the next parameter combination in the expansion recursively.
     * There are no subsequent combinations for static parameters
     */
    @Override
    public boolean advance() {

        /* This method is a no-op for static parameters */
        return false;
    }

    /**
     * @return all combinations formed from all dynamic parameters under this
     *         static parameter
     */
    @Override
    public List<String> expand(final List<String> base) {

        final List<String> results = new ArrayList<>();
        List<String> dynamicResults = new ArrayList<>();
        dynamicResults.add("");

        for (final Parameter p : this.dynamics) {

            dynamicResults = p.expand(dynamicResults);
        }

        for (final String dynamicResult : dynamicResults) {
            for (final String b : base) {
                results.add(this.parameters + " " + dynamicResult + " " + b);
            }
        }

        return results;
    }

    /**
     * @return the description of this section in the configuration file
     */
    @Override
    public String getDescription() {

        return this.description;
    }

    /**
     * @return the list of dynamic parameters contained in this static
     *         parameters section
     */
    public List<Parameter> getDynamics() {

        return this.dynamics;
    }

    /**
     * @return the static parameters string contained in this section in the
     *         configuration file
     */
    public String getParameters() {

        return this.parameters;
    }

    /**
     * @return false if this section has been fully expanded and consumed, true
     *         otherwise
     */
    @Override
    public boolean hasMore() {

        return false;
    }

    /**
     * @return the static parameters string contained in this section of the
     *         configuration in a format suitable for use on the command line
     */
    @Override
    public String print() {

        return this.parameters;
    }

    /**
     * Reset the sequence of expansion back to the start. This is not meaningful
     * for static parameters
     */
    @Override
    public void reset() {

        /* This method is a no-op for static parameters */
    }

    /**
     * @param dynamics
     *            the list of dynamic parameters
     */
    public void setDynamics(final List<Parameter> dynamics) {

        this.dynamics = dynamics;
    }

    /**
     * @param parameters
     *            the static parameters string
     */
    public void setParameters(final String parameters) {

        this.parameters = parameters;
    }

    @Override
    public String toString() {

        return this.print();
    }
}
