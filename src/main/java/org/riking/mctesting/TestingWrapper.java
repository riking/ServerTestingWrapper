package org.riking.mctesting;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

        // Pre-flight options check
        String jarfile = (String) options.valueOf("j");
        if (!new File(jarfile).exists()) {
            System.err.println("Server jar " + jarfile + " not found!");
        }

        List<TestResult> results = new ArrayList<TestResult>();

		for (Object o : options.nonOptionArguments()) {
			String inputFile = (String) o;
			File testFile = new File(inputFile);
			if (!testFile.exists()) {
				System.out.println("File " + inputFile + " does not exist!");
				results.add(new TestResult("Testing file does not exist", inputFile));
                continue;
			}

            results.add(new Tester(options, inputFile, testFile).runTest());
		}

        System.out.println(SEPARATOR);
        int tests = 0, failures = 0, errors = 0;

        for (TestResult result : results) {
            switch (result.getType()) {
                case SUCCESS:
                    tests++;
                    break;
                case FAILURE:
                    tests++; failures++;
                    System.out.println("[F] Test " + result.getTestName() + " failed:");
                    System.out.println("     " + result.getFailure());
                    break;
                case ERROR:
                    tests++; failures++; errors++;
                    System.out.println("[E] Test " + result.getTestName() + " errored:");
                    if (options.has("t")) {
                        //noinspection ThrowableResultOfMethodCallIgnored
                        result.getException().printStackTrace();
                    } else {
                        //noinspection ThrowableResultOfMethodCallIgnored
                        System.out.println("     " + result.getException().getClass().getName() + ": " + result.getException().getMessage());
                    }
            }
        }

        System.out.println(SEPARATOR);

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
