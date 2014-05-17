Before server start:

 - Properties key=value - set a value in server.properties
 - DeletePluginData plugin - delete the named folder in plugins/
 - DeleteWorld - Delete the 'world', 'world\_nether', and 'world\_the\_end' folders
 - DeleteFile filename - delete the named file from the server root
 - DeleteFileMatch regex - delete files matching the provided regex
 - DeleteFolder folder - delete the named folder from the server root
 - DeleteFolderMatch regex - delete folders matching the provided regex
 - AllowException java.lang.SomeException - ignore the named exception during server start
 - Start - starts the server

During server start:

 - WaitForStart - Pauses until the "Done" line is printed

After server start:

 - O output - expect the given (decolorized) output (The [HH:MM:SS INFO] is skipped.)
 - O* regex - expect output matching the given regex (The [HH:MM:SS INFO] is skipped.)
 - OSkip - Skip lines until the next O line is matched, or until the command completes if no O lines are provided.
 - Command command - Run a command in the server console. O lines should follow this, unless the command makes no output.
 - ConnectPlayer name - TODO
 - PlayerCommand command - TODO
 - PO, PO*, POSkip - same as above, except for messages sent to the player
 - Stop - stop the server
