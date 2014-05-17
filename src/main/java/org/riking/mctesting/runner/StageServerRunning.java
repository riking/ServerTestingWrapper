package org.riking.mctesting.runner;

import org.apache.commons.lang.StringUtils;
import org.riking.mctesting.Tester;

public class StageServerRunning extends AbstractStage {

    public StageServerRunning() {
        super("running server", AnyStageActions.getInstance(), ActiveServerActions.getInstance());
    }

    @Override
    public ActionResult doAction(Tester tester, String[] args) throws Exception {
        String command = args[0];

        if ("Command".equals(command)) {
            tester.writeLine(StringUtils.join(args, ' ', 1, args.length));

            return ActionResult.NORMAL;
        }

        return chain(tester, args);
    }
}
