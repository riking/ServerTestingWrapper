package org.riking.mctesting.runner;

public abstract class AbstractActions implements ActionHandler {

    @Override
    public final boolean eofOkay() {
        return false;
    }

    @Override
    public final String getPhaseName() {
        return null;
    }
}
