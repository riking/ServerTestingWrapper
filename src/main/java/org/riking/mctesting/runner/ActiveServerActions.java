package org.riking.mctesting.runner;

import org.riking.mctesting.Tester;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActiveServerActions extends AbstractActions {
    private static ActiveServerActions ourInstance = new ActiveServerActions();

    public static ActiveServerActions getInstance() {
        return ourInstance;
    }

    private ActiveServerActions() {
    }

    private static final Pattern messagePattern = Pattern.compile("\\[\\d\\d:\\d\\d:\\d\\d (.*?)\\]: (.*)");
    private static final String colorPattern = "§[0-9a-zA-Z]";

    private boolean outputSkipAllowed = false;

    @Override
    public ActionResult doAction(Tester tester, String[] args, String fullLine) throws Exception {
        String command = args[0];

        if (command.equals("O")) {
            String required = fullLine.substring(fullLine.indexOf(' ') + 1);

            String line;
            Matcher matcher;

            do {
                line = tester.getLine();
                matcher = messagePattern.matcher(line);
            } while (!matcher.matches() && outputSkipAllowed);

            outputSkipAllowed = false;

            if (!matcher.matches()) {
                fail(tester, line + " is not a proper message");
            }

            // String level = matcher.group(1);
            String message = matcher.group(2);
            message = message.replaceAll(colorPattern, "");

            if (!required.equals(message)) {
                fail(tester, "Expected " + required + ", got " + message);
            }
            return ActionResult.NORMAL;
        } else if (command.equals("O*")) {
            String required = fullLine.substring(fullLine.indexOf(' ') + 1);
            Pattern requiredPattern = Pattern.compile(required);

            String line;
            Matcher matcher;

            do {
                line = tester.getLine();
                matcher = messagePattern.matcher(line);
            } while (!matcher.matches() && outputSkipAllowed);

            outputSkipAllowed = false;

            if (!matcher.matches()) {
                fail(tester, line + " is not a proper message");
            }
            // String level = matcher.group(1);
            String message = matcher.group(2);

            if (!requiredPattern.matcher(message).matches()) {
                fail(tester, "Expected " + required + ", got " + message);
            }
            return ActionResult.NORMAL;
        } else if (command.equals("OSkip")) {
            outputSkipAllowed = true;
            return ActionResult.NORMAL;
        }

        return ActionResult.NOT_FOUND;
    }
}
