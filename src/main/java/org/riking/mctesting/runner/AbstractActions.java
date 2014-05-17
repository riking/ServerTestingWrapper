package org.riking.mctesting.runner;

import org.riking.mctesting.TestResult;
import org.riking.mctesting.Tester;

public abstract class AbstractActions implements ActionHandler {

    protected final void fail(Tester tester, String message) {
        tester.result = new TestResult(tester.name, message);
        tester.verbose(message);
    }

    @Override
    public final boolean eofOkay() {
        return false;
    }

    @Override
    public final String getPhaseName() {
        return null;
    }
}
