package hook;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.*;

public class MainWorker implements Runnable {

    private KafkaConsumer<String, String> consumer;

    private List<Subscriber> subscribers = new ArrayList<>();


    @Override
    public void run() {
        try {
            consumer = new KafkaConsumer<>(KafkaProperties.getKafkaProperties());
            TopicPartition partition = new TopicPartition(KafkaProperties.topic, 0);
            consumer.assign(Collections.singletonList(partition));
            consumer.seekToBeginning(Collections.singletonList(partition));
            long beginningOffset = consumer.position(partition);

            long startOffset = (AppConfig.getConfigStartOffset()!=-1) ? AppConfig.getConfigStartOffset() : AppConfig.getConfigMainLastOffset();
            if (startOffset < beginningOffset) {
                System.out.println("Start offset is too early. Starting from beginning offset: " + beginningOffset);
                consumer.seek(partition, beginningOffset);
            } else {
                consumer.seek(partition, startOffset);
            }

            while (true) {

                ManagerDB.getUrlList(subscribers);
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(5000));

                if (records.isEmpty()) continue;

                System.out.println("POLLED: " + records.count());

                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("Offset:" + record.offset() + "| New message received: " + record.value());
                    forwardToWebhooks(record.value(), record.offset());
                }
                consumer.commitSync();

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (consumer != null) consumer.close();
        }
    }

    private void forwardToWebhooks(String message, long offset) {
        try {
            for (Subscriber subscriber : subscribers) {
                if (offset <= subscriber.getOffset()) {
                    System.out.println(" Skipped: " + subscriber.getUrl() + " (offset=" + offset + " <= last_offset=" + subscriber.getOffset() + ")");
                    continue;
                }

                boolean sent = false;
                int attempts = 0;
                int statusCode = -1;

                while (!sent && attempts < 3) {
                    statusCode = WebhookSender.send(subscriber.getUrl(), message);
                    if (statusCode == 200) {
                        sent = true;
                    } else {
                        attempts++;
                        if (attempts < 3) {
                            System.out.println("Retrying (" + attempts + "): " + subscriber.getUrl());
                        }
                    }
                }

                if (sent) {
                    System.out.println("SUCCESS : " + subscriber.getUrl() + " (status: " + statusCode + ")");
                    ManagerDB.updateOffset(subscriber.getUrl(), offset);

                } else {
                    System.err.println("FAILED : " + subscriber.getUrl() + " (status: " + statusCode + ")");
                    ManagerDB.insertToFailedMessages(subscriber.getUrl(),message,offset);

                    if(AppConfig.getRetryMode().equals("unlimited")){
                        ManagerDB.deleteFromSubscribers(subscriber.getUrl());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        AppConfig.setConfigMainLastOffset(offset);
        AppConfig.saveConfig();
    }
}