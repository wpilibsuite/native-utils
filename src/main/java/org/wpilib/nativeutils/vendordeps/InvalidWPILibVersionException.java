package org.wpilib.nativeutils.vendordeps;

public class InvalidWPILibVersionException extends RuntimeException {
    static final String FORMAT = "<year>.<imageBreaking>-<WPILibBreaking>[.<WPILibNonBreaking>]";

    public InvalidWPILibVersionException(String value, Throwable cause) {
        super(message(value, cause), cause);
    }

    private static String message(String value, Throwable cause) {
        StringBuilder message = new StringBuilder("Invalid WPILib version '")
                .append(value)
                .append("'. Expected format ")
                .append(FORMAT)
                .append(" (e.g. 2027.1-1 or 2027.1-1.0).");
        if (cause instanceof NumberFormatException) {
            message.append(" One or more parts are too large.");
        }
        return message.toString();
    }
}
