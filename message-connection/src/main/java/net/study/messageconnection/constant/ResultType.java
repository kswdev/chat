package net.study.messageconnection.constant;

public enum ResultType {
    SUCCESS("Success."),
    FAILED("Failed."),
    INVALID_ARGS("Invalid Arguments."),
    NOT_FOUND("Not Found."),
    ALREADY_JOINED("Already Joined."),
    OVER_LIMIT("Over Limit."),
    NOT_JOINED("Not Joined."),
    NOT_ALLOWED("Unconnected users included."),
    ;

    private final String message;

    ResultType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
