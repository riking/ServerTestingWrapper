package org.riking.mctesting;

public class TestResult {
    private final Type type;
    private final String testName;
    private Throwable throwable;
    private String description;

    private TestResult(Type t, String name) {
        this.type = t;
        this.testName = name;
    }

    public TestResult(String name) {
        this(Type.SUCCESS, name);
    }

    public TestResult(String message, String name) {
        this(Type.FAILURE, name);
        this.description = message;
    }

    public TestResult(Throwable throwable, String name) {
        this(Type.ERROR, name);
        this.throwable = throwable;

    }

    public enum Type {
        SUCCESS,
        FAILURE,
        ERROR,
    }

    public Type getType() {
        return type;
    }

    public Throwable getException() {
        return throwable;
    }

    public String getFailure() {
        return description;
    }

    public String getTestName() {
        return testName;
    }
}
