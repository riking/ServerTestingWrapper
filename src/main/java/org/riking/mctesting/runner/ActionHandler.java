package org.riking.mctesting.runner;

public interface ActionHandler {

    public ActionResult doAction(Tester tester, String[] args) throws Exception;

    /**
     * Name of the phase of this ActionHandler. If it is a shared handler, return null.
     *
     * @return name of phase, or null if shared
     */
    public String getPhaseName();

    public enum ActionResult {
        /**
         * Normal result.
         */
        NORMAL,
        /**
         * Advance to the next stage.
         */
        NEXT_STAGE,
        /**
         * Command not found. This is an error.
         */
        NOT_FOUND,
    }
}
