package org.riking.mctesting;

import joptsimple.OptionSet;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import org.riking.mctesting.runner.*;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Tester {
    public final String name;
    public TestResult result = null;
    public Set<String> ignoredExceptions = new HashSet<String>();

    private final OptionSet optionSet;
    private BufferedReader fileReader;

    private BufferedReader outputReader;
    private OutputStreamWriter inputWriter;

    public static final Pattern stopPattern = Pattern.compile("Stopping server");
    public static final Pattern exceptionPattern = Pattern.compile("Exception");

    public Tester(OptionSet optionSet, String testName, File inputFile) {
        this.name = testName;
        this.optionSet = optionSet;
        try {
            fileReader = new BufferedReader(new FileReader(inputFile));
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
                stopServerEarly(process); // !!! ENTERED
                return result;
            }

            verbose("Stopping server...");
            writeLine("stop");
            String line;
            while ((line = getLine()) != null) {
                if (stopPattern.matcher(line).matches()) {
                    break;
                } else {
                    if (exceptionPattern.matcher(line).matches()) {
                        result = fail(line);
                        System.err.println("Dropped exception: " + line);
                    } else {
                        System.out.println("Dropped output: " + line);
                    }
                }
            }
            try {
                Thread.sleep(150L);
            } catch (InterruptedException ignored) { }
            verbose("wrote stop");

            try {
                runPhase(new StageServerShutdown());
                getMatchingLine(stopPattern);
            } catch (Throwable t) {
                // Don't exit right away - need to stop the server
                result = new TestResult(name, t);
            }

            getMatchingLine(stopPattern);
            process.waitFor();
            process = null;
            inputWriter.close();
            inputWriter = null;

            if (result != null) return result;

            runPhase(new StagePostShutdown());

            if (result != null) return result;

            return new TestResult(name);
        } catch (Throwable t) {
            System.out.println("Test error - " + t.getMessage());
            return new TestResult(name, t);
        } finally {
            if (process != null) {
                if (inputWriter != null) {
                    try {
                        writeLine("stop");
                        verbose("in finally block");
                        getMatchingLine(stopPattern);
                    } catch (IOException ignored) {
                    }
                }
                boolean interrupted = Thread.interrupted();
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                    interrupted = true;
                    System.out.println("Killed server");
                    process.destroy();
                }
                if (interrupted) Thread.currentThread().interrupt();
            }
        }
    }

    public void writeLine(String line) throws IOException {
        inputWriter.write(line + "\n");
        inputWriter.flush();
        verbose("Wrote " + line);
    }

    public String getLine() throws IOException {
        return outputReader.readLine();
    }

    public String getMatchingLine(Pattern pattern) throws IOException {
        String line;

        while ((line = getLine()) != null) {
            if (pattern.matcher(line).matches()) {
                return line;
            }
        }

        return null;
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

        Process process;
        try {
            process = builder.start();
            OutputStream stdIn = process.getOutputStream();
            InputStream stdOut = process.getInputStream();
            outputReader = new BufferedReader(new InputStreamReader(stdOut));
            inputWriter = new OutputStreamWriter(stdIn);
        } catch (IOException e) {
            throw new RuntimeException("Failed to start server: " + e.getMessage(), e);
        }
        return process;
    }

    private void stopServerEarly(Process process) throws Exception {
        verbose("Stopping server early...");
        writeLine("stop");

        verbose("Early wrote stop");
        getMatchingLine(stopPattern);
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

        while ((line = fileReader.readLine()) != null) {
            if (line.isEmpty()) continue;

            String[] args = new StrTokenizer(line, StrMatcher.splitMatcher())
                    .setTrimmerMatcher(StrMatcher.trimMatcher())
                    .getTokenArray();

            ActionHandler.ActionResult actionResult;
            try {
                actionResult = handler.doAction(this, args, line);
            } catch (ArrayIndexOutOfBoundsException e) {
                result = new TestResult(name, new IllegalArgumentException("Command `" + args[0] + "` requires more arguments", e));
                return;
            } catch (Throwable t) {
                result = new TestResult(name, t);
                return;
            }

            if (actionResult == ActionHandler.ActionResult.NEXT_STAGE) {
                // Done!
                verbose("Command successful: " + line);
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
