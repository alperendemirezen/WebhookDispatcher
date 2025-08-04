package hook;

public class Subscriber {

    private String url;
    private long offset;

    private String topic;

    public Subscriber(String url, long offset, String topic) {
        this.url = url;
        this.offset = offset;
        this.topic = topic;
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
    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return "UrlCustomer{" + "url='" + url + '\'' + ", offset=" + offset + '}';
    }

}
