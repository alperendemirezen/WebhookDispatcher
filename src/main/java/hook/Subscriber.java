package hook;

public class Subscriber {

    private String url;
    private long offset;

    public Subscriber(String url, long offset) {
        this.url = url;
        this.offset = offset;
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

    @Override
    public String toString() {
        return "UrlCustomer{" + "url='" + url + '\'' + ", offset=" + offset + '}';
    }
}
