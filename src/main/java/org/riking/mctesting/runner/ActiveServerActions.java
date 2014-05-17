package org.riking.mctesting.runner;

public class ActiveServerActions implements ActionHandler {
    private static ActiveServerActions ourInstance = new ActiveServerActions();
    public static ActiveServerActions getInstance() {
        return ourInstance;
    }
    private ActiveServerActions() { }

    @Override
    public String getPhaseName() {
        return null;
    }

    @Override
    public ActionResult doAction(Tester tester, String[] args) throws Exception {
        return null;
    }
}
