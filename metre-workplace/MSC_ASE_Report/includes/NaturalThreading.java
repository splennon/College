package org.overworld.mimic.sample.naturalthreading;

public class NaturalThreading {

    public static void main(final String... a) {
        final NaturalThreading h = new NaturalThreading();
        h.startThreads();
    }

    public void startThreads() {
        final Thread t1 = new MyThread("one");
        final Thread t2 = new MyThread("two");
        final Thread t3 = new MyThread("three");
        final Thread t4 = new MyThread("four");
        final Thread t5 = new MyThread("five");
        final Thread t6 = new MyThread("six");
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();
    }
}

class MyThread extends Thread {

    final String myName;

    public MyThread(final String n) {
        this.myName = n;
    }

    public String getMyName() {
        System.out.println("Getting name: " + this.myName);
        return this.myName;
    }

    @Override
    public void run() {
        System.out.println(this.getMyName());
    }
}
