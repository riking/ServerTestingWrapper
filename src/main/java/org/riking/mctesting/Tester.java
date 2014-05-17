package org.riking.mctesting;

import joptsimple.OptionSet;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import org.riking.mctesting.TestResult;
import org.riking.mctesting.runner.*;

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
            result = new TestResult(name, t);
        }
    }

    /**
     * Run the test. This method catches all errors.
     *
     * @return Result of the test.
     */
    public TestResult runTest() {
        if (result != null) return result;

        Process process = null;
        try {
            verbose("Entering pre-server stage...");
            runPhase(new StageBeforeServer());

            if (result != null) return result;

            verbose("Starting server...");
            process = startServer();

            try {
                runPhase(new StageServerStartup());
            } catch (Throwable t) {
                // Don't exit right away - need to stop the server
                result = new TestResult(name, t);
            }

            if (result != null) {
                stopServerEarly(process);
                return result;
            }

            verbose("Server is running. Running tests...");
            try {
                runPhase(new StageServerRunning());
            } catch (Throwable t) {
                // Don't exit right away - need to stop the server
                result = new TestResult(name, t);
            }

            if (result != null) {
                stopServerEarly(process);
                return result;
            }

            verbose("Stopping server...");
            writeLine("stop");

            try {
                runPhase(new StageServerShutdown());
            } catch (Throwable t) {
                // Don't exit right away - need to stop the server
                result = new TestResult(name, t);
            }

            process.waitFor();
            process = null;
            writerIn.close();
            writerIn = null;

            if (result != null) return result;

            runPhase(new StagePostShutdown());

            if (result != null) return result;

            return new TestResult(name);
        } catch (Throwable t) {
            System.out.println("Test error - " + t.getMessage());
            return new TestResult(name, t);
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

    private void stopServerEarly(Process process) throws Exception {
        verbose("Stopping server...");
        writeLine("stop");
        process.waitFor();
    }

    /**
     * Run a command in the pre-server phase.
     *
     * @param handler The ActionHandler for this stage
     */
    private void runPhase(ActionHandler handler) throws Exception {
        String line;

        if (handler.getPhaseName() == null) {
            result = new TestResult(name, new IllegalArgumentException("Cannot run phase on a shared handler"));
            return;
        }

        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) continue;

            String[] args = new StrTokenizer(line, StrMatcher.splitMatcher(), StrMatcher.quoteMatcher())
                    .setTrimmerMatcher(StrMatcher.trimMatcher())
                    .getTokenArray();

            ActionHandler.ActionResult actionResult;
            try {
                actionResult = handler.doAction(this, args);
            } catch (ArrayIndexOutOfBoundsException e) {
                result = new TestResult(name, new IllegalArgumentException("Command `" + args[0] + "` requires more arguments", e));
                return;
            } catch (Throwable t) {
                result = new TestResult(name, t);
                return;
            }

            if (actionResult == ActionHandler.ActionResult.NEXT_STAGE) {
                // Done!
                return;
            } else if (actionResult == ActionHandler.ActionResult.NOT_FOUND) {
                // Unrecognized commands starting with X- are ignored, otherwise, it's an error
                if (!args[0].startsWith("X-")) {
                    result = new TestResult(name, new IllegalArgumentException("Command `" + args[0] + "` is not allowed in phase " + handler.getPhaseName()));
                    return;
                }
            }

            if (result != null) {
                verbose("Command failed: " + line);
                return;
            }

            verbose("Command successful: " + line);
        }

        if (!handler.eofOkay()) {
            result = new TestResult(name, new IllegalArgumentException("Unexpected end-of-file in phase " + handler.getPhaseName()));
        }
    }

    public void verbose(String string) {
        if (optionSet.has("v")) {
            System.out.println(string);
        }
    }
}
