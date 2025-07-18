package web;

public class FailedMessage {
    private int id;
    private String url;
    private long offset;
    private String message;
    private int retry_count;
    private String last_attempt;


    // Constructors
    public FailedMessage() {
    }

    public FailedMessage(int id, String url, long offset, String message, int retry_count, String last_attempt) {
        this.id = id;
        this.url = url;
        this.offset = offset;
        this.message = message;
        this.retry_count = retry_count;
        this.last_attempt = last_attempt;

    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getRetryCount() {
        return retry_count;
    }

    public void setRetryCount(int retry_count) {
        this.retry_count = retry_count;
    }

    public String getLastAttempt() {
        return last_attempt;
    }

    public void setLastAttempt(String last_attempt) {
        this.last_attempt = last_attempt;
    }
}