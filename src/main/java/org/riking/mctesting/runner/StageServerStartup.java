package org.riking.mctesting.runner;

import org.riking.mctesting.Tester;

import java.util.regex.Pattern;

public class StageServerStartup extends AbstractStage {

    public StageServerStartup() {
        super("starting server", AnyStageActions.getInstance(), ActiveServerActions.getInstance());
    }

    @Override
    public ActionResult doAction(Tester tester, String[] args, String fullLine) throws Exception {
        String command = args[0];

        if ("WaitForStart".equals(command)) {
            Pattern pattern1 = Pattern.compile(".*? Starting minecraft server version .*");
            tester.getMatchingLine(pattern1);
            System.out.println("Server is starting.");

            Pattern pattern = Pattern.compile(".*? Done .*");
            tester.getMatchingLine(pattern);

            Thread.sleep(500);

            return ActionResult.NEXT_STAGE;
        }
        return chain(tester, args, fullLine);
    }
}
