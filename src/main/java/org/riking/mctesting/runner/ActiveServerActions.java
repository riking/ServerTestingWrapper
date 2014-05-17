package org.riking.mctesting.runner;

import org.riking.mctesting.Tester;

public class ActiveServerActions extends AbstractActions {
    private static ActiveServerActions ourInstance = new ActiveServerActions();

    public static ActiveServerActions getInstance() {
        return ourInstance;
    }

    private ActiveServerActions() {
    }

    @Override
    public ActionResult doAction(Tester tester, String[] args) throws Exception {
        return ActionResult.NOT_FOUND;
    }
}
