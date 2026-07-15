package com.zenith.client.command;

/** Result of executing a dot command. */
public class CommandResult {

    public enum Status {
        OK,
        UNKNOWN_COMMAND,
        BAD_USAGE,
        ERROR,
        NO_PERMISSION
    }

    public static final CommandResult OK = new CommandResult(Status.OK, null);
    public static final CommandResult NO_PERMISSION = new CommandResult(Status.NO_PERMISSION, "No permission");

    private final Status status;
    private final String message;

    public CommandResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public static CommandResult ok(String msg) { return new CommandResult(Status.OK, msg); }
    public static CommandResult err(String msg) { return new CommandResult(Status.ERROR, msg); }
    public static CommandResult usage(String msg) { return new CommandResult(Status.BAD_USAGE, msg); }

    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public boolean isSuccess() { return status == Status.OK; }
}
