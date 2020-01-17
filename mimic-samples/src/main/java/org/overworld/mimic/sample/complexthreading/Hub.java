package org.overworld.mimic.sample.complexthreading;

public class Hub {

    public static void main(final String... a) {

        final Hub h = new Hub();
        h.startThreads();
    }

    public void startThreads() {

        for (int i = 1; i <= 6; i++) {

            final Thread t = new MyThread("number: " + i);
            t.start();
        }
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