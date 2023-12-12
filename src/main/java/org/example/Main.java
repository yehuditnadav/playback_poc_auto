package org.example;

import org.testng.TestListenerAdapter;
import org.testng.TestNG;

public class Main {
    public static void main(String[] args) {
        System.setProperty("deviceWindowTitle", args[0]);
        System.setProperty("playbackDurationInMinutes", args[1]);
        System.setProperty("playbackPeriodInSeconds", args[2]);
        TestListenerAdapter tla = new TestListenerAdapter();
        TestNG testng = new TestNG();
        testng.setTestClasses(new Class[] { PlaybackPoc.class });
        testng.addListener(tla);
        testng.run();
    }
}