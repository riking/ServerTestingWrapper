package org.riking.mctesting.runner;

import joptsimple.OptionSet;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import org.riking.mctesting.TestResult;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class Tester {
    public final String name;
    public TestResult result = null;
    public Set<String> ignoredExceptions = new HashSet<String>();

    private final OptionSet optionSet;
    private BufferedReader reader;

    private BufferedReader readerOut;
    private OutputStreamWriter writerIn;

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

        Process process = null;
        try {
            System.out.println("[" + name + "] STARTING TEST");
            verbose("Entering pre-server stage...");
            runPhase(new StageBeforeServer());

            if (result != null) return result;

            verbose("Starting server...");
            process = startServer();
            runPhase(StageServerStartup.getInstance());

            writeLine("stop");
            process.waitFor();
            writerIn.close();
            readerOut.close();

            System.out.println("[" + name + "] TEST COMPLETE");
            return new TestResult(name);
        } catch (Throwable t) {
            System.out.println("Test errored - " + t.getMessage());
            return new TestResult(t, name);
        } finally {
            if (process != null) {
                if (writerIn != null) {
                    try {
                        writeLine("stop");
                    } catch (IOException ignored) {
                    }
                }
                boolean interrupted = Thread.interrupted();
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    interrupted = true;
                    process.destroy();
                }
                if (interrupted) Thread.currentThread().interrupt();
            }
        }
    }

    public void writeLine(String line) throws IOException {
        writerIn.write(line);
        writerIn.flush();
    }

    public String getLine() throws IOException {
        return readerOut.readLine();
    }

    private Process startServer() {
        ProcessBuilder builder = new ProcessBuilder(
                "java",
                "-Xmx" + optionSet.valueOf("memory"),
                "-XX:MaxPermSize=128M",
                "-jar",
                (String) optionSet.valueOf("jar"),
                "-nojline"
        );

        builder.redirectErrorStream();

        Process process;
        try {
            process = builder.start();
            OutputStream stdIn = process.getOutputStream();
            InputStream stdOut = process.getInputStream();
            readerOut = new BufferedReader(new InputStreamReader(stdOut));
            writerIn = new OutputStreamWriter(stdIn);
        } catch (IOException e) {
            throw new RuntimeException("Failed to start server: " + e.getMessage(), e);
        }
        return process;
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
                    // Unrecognized commands starting with X- are ignored, otherwise, it's an error
                    if (!args[0].startsWith("X-")) {
                        throw new IllegalArgumentException("Command `" + args[0] + "` is not allowed in phase " + handler.getPhaseName());
                    }
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
