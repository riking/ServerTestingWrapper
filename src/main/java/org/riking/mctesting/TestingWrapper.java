package org.riking.mctesting;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.codehaus.plexus.util.ExceptionUtils;
import org.riking.mctesting.runner.Tester;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestingWrapper {

    // 50 equals signs
    public static final String SEPARATOR = "==================================================";

    public static void main(String[] args) {
        OptionParser parser = new OptionParser() {
            {
                acceptsAll(asList("?", "h", "help"), "Show the help");

                acceptsAll(asList("m", "memory"), "Memory to allocate (-Xmx)")
                        .withRequiredArg()
                        .defaultsTo("2G");

                acceptsAll(asList("v", "verbose"), "Verbose output");

                acceptsAll(asList("q", "quiet"), "Only output test counts, not the failure details");

                acceptsAll(asList("t", "trace"), "Output stacktrace for exceptions");

                acceptsAll(asList("j", "jar"), "Name of CraftBukkit jar")
                        .withOptionalArg()
                        .ofType(String.class)
                        .defaultsTo("craftbukkit.jar");

                nonOptions("Test script files").ofType(String.class);
            }
        };

        OptionSet options = null;

        try {
            options = parser.parse(args);
        } catch (joptsimple.OptionException ex) {
            System.err.println("Error: " + ex.getMessage());
            System.exit(3);
        }

        if ((options == null) || (options.has("?"))) {
            try {
                parser.printHelpOn(System.out);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.exit(3);
        }

        // Pre-flight options parsing

        String jarfile = (String) options.valueOf("j");
        if (!new File(jarfile).exists()) {
            System.err.println("Server jar " + jarfile + " not found!");
            System.exit(3);
        }

        if (options.has("q")) {
            // System.err only
            System.setOut(new PrintStream(new NullOutputStream()));
        }

        // Start testing

        List<TestResult> results = new ArrayList<TestResult>();

        for (Object o : options.nonOptionArguments()) {
            String inputFile = (String) o;
            File testFile = new File(inputFile);
            if (!testFile.exists()) {
                System.out.println("File " + inputFile + " does not exist!");
                results.add(new TestResult("Testing file does not exist", inputFile));
                continue;
            }

            System.out.println("[" + testFile.getName() + "] STARTING TEST");
            results.add(new Tester(options, inputFile, testFile).runTest());
            System.out.println("[" + testFile.getName() + "] TEST COMPLETE");
        }

        System.out.println(SEPARATOR);
        int tests = 0, failures = 0, errors = 0;
        StringBuilder buffer = new StringBuilder();

        for (TestResult result : results) {
            switch (result.getType()) {
                case SUCCESS:
                    tests++;
                    System.out.print('.');
                    break;
                case FAILURE:
                    tests++;
                    failures++;
                    System.out.print('F');

                    buffer.append("[F] Test ").append(result.getTestName()).append(" failed:").append('\n');
                    buffer.append("     ").append(result.getFailure()).append('\n');
                    break;
                case ERROR:
                    tests++;
                    failures++;
                    errors++;
                    System.out.print('E');

                    buffer.append("[E] Test ").append(result.getTestName()).append(" errored:").append('\n');
                    if (options.has("t")) {
                        buffer.append(ExceptionUtils.getStackTrace(result.getException())).append('\n');
                    } else {
                        //noinspection ThrowableResultOfMethodCallIgnored
                        buffer.append("     ")
                                .append(result.getException().getClass().getName())
                                .append(": ")
                                .append(result.getException().getMessage());
                        //noinspection ThrowableResultOfMethodCallIgnored
                        if (result.getException().getCause() != null) {
                            //noinspection ThrowableResultOfMethodCallIgnored
                            buffer.append("     Caused by: ")
                                    .append(result.getException().getClass().getName())
                                    .append(": ")
                                    .append(result.getException().getMessage());
                        }
                    }
            }
        }
        System.out.print("\n\n");

        System.out.println(buffer.toString());

        if (failures == 0) {
            System.out.println("Ran " + tests + " tests, no failures.");
            System.exit(0);
        } else if (errors == 0) {
            System.err.println(tests + " tests run, " + failures + " tests failed.");
            System.exit(1);
        } else {
            System.err.println(tests + " tests run, " + failures + " tests failed, " + errors + " tests with errors.");
            System.exit(2);
        }
    }

    private static List<String> asList(String... params) {
        return Arrays.asList(params);
    }
}
