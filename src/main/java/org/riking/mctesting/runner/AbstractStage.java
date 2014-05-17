package org.riking.mctesting.runner;

public abstract class AbstractStage implements ActionHandler {
    private String name;
    private ActionHandler[] chainedHandlers;

    public AbstractStage(String name, ActionHandler... chained) {
        this.name = name;
        this.chainedHandlers = chained;
    }

    protected final ActionResult chain(Tester tester, String[] args) throws Exception {
        for (ActionHandler handler : chainedHandlers) {
            if (handler.doAction(tester, args) != ActionResult.NOT_FOUND) {
                return ActionResult.NORMAL;
            }
        }

        return ActionResult.NOT_FOUND;
    }

    @Override
    public boolean eofOkay() {
        return false;
    }

    @Override
    public final String getPhaseName() {
        return name;
    }
}
