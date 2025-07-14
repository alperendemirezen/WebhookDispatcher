package hook;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class WebhookSender {

    public static int send(String url, String payload) {
        int timeout;

        try{
            String configTimeout = AppConfig.getConfig().getProperty("timeoutMS");
            timeout = (configTimeout != null && !configTimeout.isEmpty()) ? Integer.parseInt(configTimeout) : 1000;
        }catch (NumberFormatException e){
            timeout = 1000;
        }

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {

            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(payload));

            try (CloseableHttpResponse response = client.execute(post)) {
                return response.getStatusLine().getStatusCode();
            }

        } catch (Exception e) {
            System.err.println("SEND ERROR → " + url + ": " + e.getMessage());
            return -1;
        }
    }
}
