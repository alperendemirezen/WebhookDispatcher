package web;

public class PrivateSubscriber {
    private int id;
    private String url;
    private long last_offset;


    public PrivateSubscriber(int id, String url, long last_offset) {
        this.id = id;
        this.url = url;
        this.last_offset = last_offset;
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

    public long getOffset() {
        return last_offset;
    }

    public void setOffset(long last_offset) {
        this.last_offset = last_offset;
    }
}