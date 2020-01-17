package org.overworld.mimic.sample.conplexcomponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;

public class InstrumentMe {

    public String junkRecordingButtonGroup() {

        final ButtonGroup bg = new ButtonGroup();

        System.out.println("Creating a button and adding to the button group");
        bg.add(new JButton("A Button"));

        return bg.getElements().nextElement().getText();
    }

    public int junkReturningInt() {

        // Make a List of all anagram groups above size threshold.
        final List<List<String>> winners = new ArrayList<List<String>>();

        // Sort anagram groups according to size
        Collections.sort(winners, new Comparator<List<String>>() {
            @Override
            public int compare(final List<String> o1, final List<String> o2) {

                return o2.size() - o1.size();
            }
        });

        // Print anagram groups.
        for (final List<String> l : winners)
            System.out.println(l.size() + ": " + l);

        System.out.println("HI!");
        return 42;
    }
}
