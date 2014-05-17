package org.riking.mctesting.runner;

public class AnyStageActions extends AbstractActions {

    private static AnyStageActions ourInstance = new AnyStageActions();

    public static AnyStageActions getInstance() {
        return ourInstance;
    }

    private AnyStageActions() {
    }

    @Override
    public ActionResult doAction(Tester tester, String[] args) throws Exception {
        String command = args[0];

        if ("AllowException".equals(command)) {
            tester.ignoredExceptions.add(args[1]);
            return ActionResult.NORMAL;
        } else if ("StopAllowException".equals(command)) {
            tester.ignoredExceptions.remove(args[1]);
            return ActionResult.NORMAL;
        }

        return ActionResult.NOT_FOUND;
    }
}
