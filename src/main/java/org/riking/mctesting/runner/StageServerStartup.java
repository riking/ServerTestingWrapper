package org.riking.mctesting.runner;

import org.riking.mctesting.Tester;

public class StageServerStartup extends AbstractStage {

    public StageServerStartup() {
        super("starting server", AnyStageActions.getInstance(), ActiveServerActions.getInstance());
    }

    @Override
    public ActionResult doAction(Tester tester, String[] args) throws Exception {
        String command = args[0];

        return chain(tester, args);
    }
}
