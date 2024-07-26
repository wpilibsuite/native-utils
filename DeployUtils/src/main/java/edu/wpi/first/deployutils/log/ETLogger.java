package edu.wpi.first.deployutils.log;

import java.util.concurrent.Semaphore;
import org.gradle.api.Action;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.internal.logging.text.StyledTextOutput;

public class ETLogger {
    private int indent;

    public int getIndent() {
        return indent;
    }

    public void setIndent(int indent) {
        this.indent = indent;
    }

    private String indentStr;

    public String getIndentStr() {
        return indentStr;
    }

    public void setIndentStr(String indentStr) {
        this.indentStr = indentStr;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private boolean silent;

    public boolean getSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    private Logger internalLogger;

    public Logger getInternalLogger() {
        return internalLogger;
    }

    public void setInternalLogger(Logger internalLogger) {
        this.internalLogger = internalLogger;
    }

    private StyledTextOutput colorOut;

    public StyledTextOutput getColorOut() {
        return colorOut;
    }

    public void setColorOut(StyledTextOutput colorOut) {
        this.colorOut = colorOut;
    }

    private Semaphore semaphore;

    public ETLogger(String name, StyledTextOutput textOutput, int indent) {
        this.name = name;
        this.indent = indent;
        this.indentStr = "";
        for (int i = 0; i < indent; i++) {
            indentStr += ' ';
        }
        this.internalLogger = Logging.getLogger(name);
        this.colorOut = textOutput;
        this.semaphore = new Semaphore(1);
    }

    public ETLogger(String name, StyledTextOutput textOutput) {
        this(name, textOutput, 0);
    }

    public ETLogger push() {
        return new ETLogger(name, colorOut, indent + 2);
    }

    public void withLock(Action<ETLogger> c) {
        try {
            this.semaphore.acquire();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        try {

            c.execute(this);
        } finally {
            this.semaphore.release();
        }
    }

    public void log(String msg) {
        if (!silent) System.out.println(indentStr + msg);

        if (internalLogger.isInfoEnabled()) {
            internalLogger.info("Log " + (silent ? "[silent]" : "") + ": " + indentStr + msg);
        }
    }

    public void info(String msg) {
        internalLogger.info(msg);
    }

    public void debug(String msg) {
        internalLogger.debug(msg);
    }

    public Logger backingLogger() {
        return internalLogger;
    }

    public void logStyle(String msg, StyledTextOutput.Style style) {
        if (colorOut != null) {
            colorOut.withStyle(style).println(indentStr + msg);
        } else {
            log(msg);
        }
    }

    public void logError(String msg) {
        logStyle(msg, StyledTextOutput.Style.Failure);
    }


    public void logErrorHead(String msg) {
        logStyle(msg, StyledTextOutput.Style.FailureHeader);
    }


    public void silent(boolean value) {
        silent = value;
    }
}
