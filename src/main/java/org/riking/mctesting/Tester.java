package org.riking.mctesting;

import joptsimple.OptionSet;

import java.io.File;

public class Tester {
    private final String name;
    private final OptionSet optionSet;
    private TestResult result = null;

    public Tester(OptionSet optionSet, String testName, File inputFile) {
        this.name = testName;
        this.optionSet = optionSet;
        try {

        } catch (Throwable t) {
            result = new TestResult(t, name);
        }
    }

    public TestResult runTest() {
        if (result != null) return result;

        try {

            return new TestResult(name);
        } catch (Throwable t) {
            return new TestResult(t, name);
        }
    }
}
