package org.riking.mctesting.runner;

import joptsimple.OptionSet;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import org.riking.mctesting.TestResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

public class Tester {
    public final String name;
    public TestResult result = null;
    public Set<String> ignoredExceptions = new HashSet<String>();

    private final OptionSet optionSet;
    private BufferedReader reader;

    public Tester(OptionSet optionSet, String testName, File inputFile) {
        this.name = testName;
        this.optionSet = optionSet;
        try {
            reader = new BufferedReader(new FileReader(inputFile));
        } catch (Throwable t) {
            result = new TestResult(t, name);
        }
    }

    private boolean fileEmpty = true;

    /**
     * Run the test. This method catches all errors.
     *
     * @return Result of the test.
     */
    public TestResult runTest() {
        if (result != null) return result;

        try {
            verbose("Entering pre-server stage...");
            runPhase(new PreServerActions());

            if (result != null) return result;

            verbose("Starting server...");

            return new TestResult(name);
        } catch (Throwable t) {
            System.out.println("Catching");
            return new TestResult(t, name);
        }
    }

    /**
     * Run a command in the pre-server phase.
     *
     * @param handler The ActionHandler for this stage
     */
    private void runPhase(ActionHandler handler) throws Exception {
        String line;

        if (handler.getPhaseName() == null) {
            throw new IllegalArgumentException("Cannot run phase on a shared handler");
        }

        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) continue;

            String[] args = new StrTokenizer(line, StrMatcher.splitMatcher(), StrMatcher.quoteMatcher())
                    .setTrimmerMatcher(StrMatcher.trimMatcher())
                    .getTokenArray();

            try {
                ActionHandler.ActionResult actionResult = handler.doAction(this, args);
                if (actionResult == ActionHandler.ActionResult.NEXT_STAGE) {
                    // Done!
                    return;
                } else if (actionResult == ActionHandler.ActionResult.NOT_FOUND) {
                    throw new IllegalArgumentException("Command `" + args[0] + "` is not allowed in phase " + handler.getPhaseName());
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Command `" + args[0] + "` requires more arguments", e);
            }

            if (result != null) return;

            verbose("Command successful: " + line);
        }

        throw new IllegalArgumentException("Unexpected end-of-file in phase " + handler.getPhaseName());
    }

    public void verbose(String string) {
        if (optionSet.has("v")) {
            System.out.println(string);
        }
    }
}
