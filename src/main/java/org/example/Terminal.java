package org.example;

import il.co.topq.integframework.cli.conn.CliCommand;

public class Terminal extends CmdConnection {
    private static long defaultTimeOut = 30000;
    private long timeout = defaultTimeOut;
    private String promptString = "$";

    @Override
    public void init() throws Exception {
        setConnectOnInit(false);
        super.init();
    }

    private boolean isWindowsOS() {
        return (System.getProperty("os.name").startsWith("Windows"));
    }

    public void viewFile(String title, String fullFileName) throws Exception {
        String viewCommand = isWindowsOS() ? "type " : "cat ";
        handleCliCommand(title, viewCommand + fullFileName);
    }

    public void sendCommand(String command) throws Exception {
        handleCliCommand(command, command);
    }

    public void handleCliCommand(String title, String[] commands) throws  Exception {
        CliCommand cmd = new CliCommand();
        addErrors(cmd);
        cmd.setCommands(commands);
        handleCliCommand(title, cmd);
    }

    public void handleCliCommand(String title, CliCommand command) throws  Exception {
        if (!isConnected()) {
            connect();
        }
        // set customized timeout - from terminal setting to from command setting
        timeout = timeout != defaultTimeOut ? timeout : command.getTimeout();
        command.setTimeout(timeout);
        super.handleCliCommand(title, command);
    }

    public void handleCliCommand(String title, String command) throws  Exception {
        if (!isConnected()) {
            connect();
        }
        if (!isWindowsOS()) {
            command = command + "; echo -e \"\n" + promptString + "\"";
        }
        CliCommand cmd = new CliCommand(command);
        addErrors(cmd);
        cmd.setTimeout(timeout);
        super.handleCliCommand(title, cmd);

    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getPromptString() {
        return promptString;
    }

    public void setPromptString(String promptString) {
        this.promptString = promptString;
    }

    private void addErrors (CliCommand cmd) {
//        cmd.addErrors("Failed");
        cmd.addErrors("command not found");
    }
}
