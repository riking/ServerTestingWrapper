package org.riking.mctesting.runner;

public interface ActionHandler {

    /**
     * Name of the phase of this ActionHandler. If it is a shared handler, return null.
     *
     * @return name of phase, or null if shared
     */
    public String getPhaseName();

    /**
     * Check whether end-of-file is acceptable at this stage.
     *
     * @return true if EOF is okay
     */
    public boolean eofOkay();

    public ActionResult doAction(Tester tester, String[] args) throws Exception;

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
