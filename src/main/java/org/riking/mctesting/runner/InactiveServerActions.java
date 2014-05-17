package org.riking.mctesting.runner;

import org.codehaus.plexus.util.FileUtils;
import org.riking.mctesting.Tester;

import java.io.File;

public class InactiveServerActions extends AbstractActions {
    private static InactiveServerActions ourInstance = new InactiveServerActions();

    public static InactiveServerActions getInstance() {
        return ourInstance;
    }

    private InactiveServerActions() {
    }

    @Override
    public ActionResult doAction(Tester tester, String[] args) throws Exception {
        String command = args[0];

        if ("DeleteWorlds".equals(command)) {
            FileUtils.deleteDirectory(new File("world"));
            FileUtils.deleteDirectory(new File("world_nether"));
            FileUtils.deleteDirectory(new File("world_the_end"));

            tester.verbose("Deleted world, nether, end");

            File[] directories = new File(".").listFiles();
            if (directories != null) {
                for (File file : directories) {
                    if (!file.isDirectory()) continue;

                    if (new File(file, "level.dat").exists()) {
                        FileUtils.deleteDirectory(file);
                        tester.verbose("Deleted " + file.getName());
                    }
                }
            }

            return ActionResult.NORMAL;
        } else if ("Delete".equals(command)) {
            if (new File(args[1]).delete()) {
                tester.verbose("Deleted file");
            }
            return ActionResult.NORMAL;
        } else if ("DeleteFolder".equals(command)) {
            FileUtils.deleteDirectory(new File(args[1]));

            return ActionResult.NORMAL;
        } else if ("Copy".equals(command)) {
            File from = new File(args[1]);
            File to = new File(args[2]);
            FileUtils.copyFile(from, to);

            return ActionResult.NORMAL;
        } else if ("CopyFolder".equals(command)) {
            File from = new File(args[1]);
            File to = new File(args[2]);
            FileUtils.copyDirectory(from, to);

            return ActionResult.NORMAL;
        }

        return ActionResult.NOT_FOUND;
    }
}
