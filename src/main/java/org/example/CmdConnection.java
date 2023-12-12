package org.example;

import il.co.topq.difido.ReportManager;
import il.co.topq.difido.model.Enums;
import il.co.topq.integframework.cli.conn.CliCommand;
import il.co.topq.integframework.cli.conn.CliConnectionImpl;
import il.co.topq.integframework.cli.conn.Position;
import il.co.topq.integframework.cli.terminal.Cli;
import il.co.topq.integframework.cli.terminal.Prompt;

import java.util.ArrayList;

/**
 * Created by yehuditnadav on 21/06/2016.
 */
public class CmdConnection extends CliConnectionImpl {

    private String dir;
    private boolean cloneOnEveryOperation;
    protected String lastCommandResult;

    protected ReportManager report = ReportManager.getInstance();
    public CmdConnection() {
    }

    public CmdConnection(String dir) {
        this();
    }

    public void init() throws Exception {
        super.init();
        setCloneOnEveryOperation(true);
    }

    public void connect() throws Exception {
        this.setCloneOnEveryOperation(false);
        this.terminal = new Cmd(this.dir);
        ((Cmd)this.terminal).setCloseOutputOnSend(this.isCloneOnEveryOperation());
        this.terminal.setPrompts(this.internalGetPrompts());
        this.cli = new Cli(this.terminal);
        this.setEnterStr("\n");
    }

    boolean isReportSilent = false;

    public void handleCliCommand(String title, CliCommand command) throws Exception {
        boolean commandClone = command.isClone();
        boolean isCloneOnEveryOp = this.isCloneOnEveryOperation();
        if(this.isCloneOnEveryOperation()) {
            command.setClone(true);
        }

        if(command.isClone()) {
            this.setCloneOnEveryOperation(true);
        }

        boolean status = true;
        try {
            // save report silent, in order to check it before generate report in finally block
            isReportSilent = command.isSilent();
            // set report silent as true anyway, because we want to generate report in finally block
            command.setSilent(true);
            super.handleCliCommand(title, command);
        } catch (Exception e) {
            status = false;
            throw e;
        } finally {
            if (!isReportSilent)
                report.logHtml(title,
                        command.getCommands()[0] + "\n" + command.getResult(),
                        (status ? Enums.Status.success : Enums.Status.failure));
            command.setClone(commandClone);
            this.setCloneOnEveryOperation(isCloneOnEveryOp);
            lastCommandResult = command.getResult().substring(
                    command.getResult().indexOf(command.getCommands()[0]) + command.getCommands()[0].length());
        }
    }

    public Position[] getPositions() {
        return null;
    }

    public Prompt[] getPrompts() {
        return (Prompt[])this.internalGetPrompts().toArray(new Prompt[0]);
    }

    private ArrayList<Prompt> internalGetPrompts() {
        ArrayList prompts = new ArrayList();
        new Prompt();
        Prompt p = new Prompt();
        p.setPrompt("$");
        p.setCommandEnd(true);
        prompts.add(p);
        p = new Prompt();
        p.setPrompt("#");
        p.setCommandEnd(true);
        prompts.add(p);
        p = new Prompt();
        p.setPrompt(">");
        p.setCommandEnd(true);
        prompts.add(p);
        p = new Prompt();
        p.setPrompt("~");
        p.setCommandEnd(true);
        prompts.add(p);
        return prompts;
    }

    public String getDir() {
        return this.dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public boolean isCloneOnEveryOperation() {
        return this.cloneOnEveryOperation;
    }

    public void setCloneOnEveryOperation(boolean resetOnEveryOperation) {
        this.cloneOnEveryOperation = resetOnEveryOperation;
    }

    public String getLastCommandResult() {
        return lastCommandResult;
    }

    public void setLastCommandResult(String lastCommandResult) {
        this.lastCommandResult = lastCommandResult;
    }

    public Object clone() throws CloneNotSupportedException {
        CmdConnection connection = (CmdConnection)super.clone();
        connection.connectOnInit = false;
        connection.cloneOnEveryOperation = this.cloneOnEveryOperation;
        return connection;
    }

}
