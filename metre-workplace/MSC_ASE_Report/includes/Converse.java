package org.overworld.mimic.sample.converse;

public class Converse {

    public static void main(final String[] args) {

        System.out.println("Hello, what is your name?");
        final String name = System.console().readLine();
        System.out.println(String.format("Hello %s!", name));

        System.out.println("How are you today?");
        final String feeling = System.console().readLine();
        System.out.println(String.format(
            "I'm glad you are feeling %s today %s", feeling, name));

        System.out.println(String.format("What food do you like to eat when "
            + "you are feeling %s %s?", feeling, name));
        final String food = System.console().readLine();
        System.out.println(String.format(
            "Being a Java program, I rarely eat %s,"
                + " but I am glad you like it %s.", food, name));
    }
}
