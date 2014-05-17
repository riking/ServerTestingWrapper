package org.riking.mctesting.runner;

import org.riking.mctesting.Tester;

public class StagePostShutdown extends AbstractStage {

    public StagePostShutdown() {
        super("stopped server", AnyStageActions.getInstance(), InactiveServerActions.getInstance());
    }

    @Override
    public boolean eofOkay() {
        return true;
    }

    @Override
    public ActionResult doAction(Tester tester, String[] args, String fullLine) throws Exception {
        String command = args[0];

        return chain(tester, args, fullLine);
    }
}
