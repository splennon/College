package org.overworld.metre.config;

import java.util.ArrayList;
import java.util.List;

/**
 * A POJO to represent the high level sections from the configuration file file
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2016-02-14
 */
public class ConfigRoot {

    private List<Parameter> dynamics = new ArrayList<>();
    private JMeter jmeterOptions;
    private MetreOptions metreOptions;
    private List<StaticParameters> statics = new ArrayList<>();
    private Target targetOptions;

    /**
     * @return
     */
    public List<String> expand() {

        final List<String> staticResults = new ArrayList<>();

        List<String> dynamicResults = new ArrayList<>();
        dynamicResults.add("");

        for (final Parameter dp : this.dynamics) {

            dynamicResults = dp.expand(dynamicResults);
        }

        for (final StaticParameters sp : this.statics) {
            staticResults.addAll(sp.expand(dynamicResults));
        }

        return staticResults;
    }

    /**
     * @return a list of all Parameters
     */
    public List<Parameter> getDynamics() {

        return this.dynamics;
    }

    public JMeter getJmeterOptions() {

        return this.jmeterOptions;
    }

    public MetreOptions getMetreOptions() {

        return this.metreOptions;
    }

    public List<StaticParameters> getStatics() {

        return this.statics;
    }

    public Target getTargetOptions() {

        return this.targetOptions;
    }

    public void setDynamics(final List<Parameter> dynamics) {

        this.dynamics = dynamics;
    }

    public void setJmeterOptions(final JMeter jmeterOptions) {

        this.jmeterOptions = jmeterOptions;
    }

    public void setMetreOptions(final MetreOptions metreOptions) {

        this.metreOptions = metreOptions;
    }

    public void setStatics(final List<StaticParameters> statics) {

        this.statics = statics;
    }

    public void setTargetOptions(final Target targetOptions) {

        this.targetOptions = targetOptions;
    }

    @Override
    public String toString() {

        return "ConfigRoot [dynamics=" + this.dynamics + ", jmeterOptions="
            + this.jmeterOptions + ", metreOptions=" + this.metreOptions
            + ", statics=" + this.statics + ", targetOptions="
            + this.targetOptions + "]";
    }
}
