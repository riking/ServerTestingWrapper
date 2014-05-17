package org.riking.mctesting.runner;

public class StagePostShutdown implements ActionHandler {

    private static ActionHandler[] chainedHandlers = new ActionHandler[] {
            AnyStageActions.getInstance(),
            InactiveServerActions.getInstance(),
    };

    @Override
    public String getPhaseName() {
        return "stopped server";
    }

    @Override
    public ActionResult doAction(Tester tester, String[] args) throws Exception {
        String command = args[0];

        for (ActionHandler handler : chainedHandlers) {
            if (handler.doAction(tester, args) != ActionResult.NOT_FOUND) {
                return ActionResult.NORMAL;
            }
        }

        return ActionResult.NOT_FOUND;
    }
}
