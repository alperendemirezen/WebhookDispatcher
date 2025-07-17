package hook;

public class FailedMessage {
    private int id;
    private String url;
    private String message;
    private long offset;
    private int retryCount;
    private String lastAttempt;

    // Constructor
    public FailedMessage(int id, String url, String message, long offset, int retryCount, String lastAttempt) {
        this.id = id;
        this.url = url;
        this.message = message;
        this.offset = offset;
        this.retryCount = retryCount;
        this.lastAttempt = lastAttempt;
    }


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

    public String getMessage() {
        return message;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public int getRetryCount() {
        return retryCount;
    }
}