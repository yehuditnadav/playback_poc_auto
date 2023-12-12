package org.example;

import il.co.topq.integframework.utils.StringUtils;
import il.co.topq.integframework.cli.terminal.Terminal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
public class Cmd extends Terminal {
    private String processDir;
    private Process process;
    private boolean closeOutputOnSend;

    public Cmd() {
    }

    public Cmd(String dir) {
        this.processDir = dir;
    }

    public void connect() throws IOException {
        File root = null;
        if(!StringUtils.isEmpty(this.processDir)) {
            root = new File(this.processDir);
        }
        ProcessBuilder builder;
        if (System.getProperty("os.name").startsWith("Windows")){
            builder = new ProcessBuilder(new String[]{"cmd.exe"});
        } else {
            builder = new ProcessBuilder(new String[]{"/bin/bash"});
        }

        builder.directory(root);
        builder.redirectErrorStream(true);
        this.process = builder.start();
        this.in = this.process.getInputStream();
        this.out = new BufferedOutputStream(this.process.getOutputStream());
    }

    public synchronized void sendString(String command, boolean delayedTyping) throws IOException, InterruptedException {
        super.sendString(command, delayedTyping);
        if(this.isCloseOutputOnSend()) {
            this.out.close();
        }

    }

    public void disconnect() throws IOException {
        this.process.destroy();
    }

    public boolean isConnected() {
        if(this.process == null) {
            return false;
        } else {
            try {
                this.process.exitValue();
                return false;
            } catch (IllegalThreadStateException var2) {
                return true;
            }
        }
    }

    public String getConnectionName() {
        return "cmd";
    }

    private boolean isCloseOutputOnSend() {
        return this.closeOutputOnSend;
    }

    public void setCloseOutputOnSend(boolean closeOutPutOnSend) {
        this.closeOutputOnSend = closeOutPutOnSend;
    }
}