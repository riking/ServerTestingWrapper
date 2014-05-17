package org.riking.mctesting.runner;

import org.riking.mctesting.Tester;

public class StageServerShutdown extends AbstractStage {

    public StageServerShutdown() {
        super("stopping server", AnyStageActions.getInstance(), ActiveServerActions.getInstance());
    }

    @Override
    public boolean eofOkay() {
        return true;
    }

    @Override
    public ActionResult doAction(Tester tester, String[] args) throws Exception {
        String command = args[0];

        return chain(tester, args);
    }
}
