package dev.thorinwasher.forgery.util;

import java.util.logging.Level;

public class Logger {

    public static void logInfo(String message) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
        String prefixedMessage = "[Forgery Error - " + className + ":" + caller.getLineNumber() + "] " + message;
        java.util.logging.Logger.getLogger("Forgery").log(Level.INFO, prefixedMessage);
    }

    public static void logWarn(String message) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
        String prefixedMessage = "[Forgery Debug - " + className + ":" + caller.getLineNumber() + "] " + message;
        java.util.logging.Logger.getLogger("Forgery").log(Level.WARNING, prefixedMessage);
    }

    public static void logErr(String message) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
        String prefixedMessage = "[Forgery Error - " + className + ":" + caller.getLineNumber() + "] " + message;
        java.util.logging.Logger.getLogger("Forgery").log(Level.SEVERE, prefixedMessage);
    }
}
