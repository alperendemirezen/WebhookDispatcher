package hook;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import log.Log4jUtility;

import java.time.Duration;
import java.util.*;

public class MainWorker implements Runnable {

    private KafkaConsumer<String, String> consumer;
    private List<Subscriber> subscribers = new ArrayList<>();
    private String mode;
    private long lastOffset;
    private String topic;

    public MainWorker(String topic) {
        this.topic = topic;
    }

    @Override
    public void run() {
        try {

            mode = AppConfig.getRetryMode();
            Log4jUtility.getLogger().debug("Mode: " + AppConfig.getRetryMode());
            Log4jUtility.getLogger().debug("Started on thread: " + Thread.currentThread().getName());
            ThreadStatusManager.registerThread();

            consumer = new KafkaConsumer<>(KafkaProperties.getKafkaProperties());
            TopicPartition partition = new TopicPartition(topic, 0);
            consumer.assign(Collections.singletonList(partition));
            consumer.seekToBeginning(Collections.singletonList(partition));
            long beginningOffset = consumer.position(partition);

            long startOffset = (OffsetConfig.getMainOffset(topic) != -1) ? OffsetConfig.getMainOffset(topic) : lastOffset;
            if (startOffset < beginningOffset) {
                Log4jUtility.getLogger().debug("Start offset is too early. Starting from beginning offset: " + beginningOffset);
                consumer.seek(partition, beginningOffset);
            } else {
                consumer.seek(partition, startOffset);
            }

            while (true) {

                ManagerDB.getSubscribers(subscribers, topic);
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(5000));

                if (records.isEmpty()) {
                    if (records.isEmpty()) Log4jUtility.getLogger().debug("Record is empty");
                    PauseController.waitIfPaused();
                    continue;
                }

                Log4jUtility.getLogger().debug("POLLED: " + records.count());

                for (ConsumerRecord<String, String> record : records) {
                    if (!Thread.currentThread().isInterrupted()) {
                        Log4jUtility.getLogger().debug("MAIN : Offset:" + record.offset() + "| New message received: " + record.value());
                        forwardToWebhooks(record.value(), record.offset());
                        PauseController.waitIfPaused();
                        if (mode.equals("unlimited")) {
                            ManagerDB.getSubscribers(subscribers, topic); //SEDAT ABIYE SOR
                        }
                    }
                }
                consumer.commitSync();
            }
        } catch (Exception e) {
            Log4jUtility.getLogger().error("Error in MainWorker: " + e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (consumer != null) consumer.close();
            ThreadStatusManager.unregisterThread();
        }
    }

    private void forwardToWebhooks(String message, long offset) {
        try {
            for (Subscriber subscriber : subscribers) {
                if (offset <= subscriber.getOffset()) {
                    Log4jUtility.getLogger().debug(" Skipped: " + subscriber.getUrl() + " (offset=" + offset + " <= last_offset=" + subscriber.getOffset() + ")");
                    continue;
                }

                boolean sent = false;
                int attempts = 0;
                int statusCode = -1;
                int attemptsLimit = (mode.equals("unlimited")) ? 3 : 1;

                while (!sent && attempts < attemptsLimit) {
                    statusCode = WebhookSender.send(subscriber.getUrl(), message, offset);
                    if (statusCode == 200) {
                        sent = true;
                    } else {
                        attempts++;
                        if (attempts < attemptsLimit) {
                            Log4jUtility.getLogger().debug("Retrying (" + attempts + "): " + subscriber.getUrl());
                        }
                    }
                }

                if (sent) {
                    Log4jUtility.getLogger().debug("SUCCESS : " + subscriber.getUrl() + " (status: " + statusCode + ")");
                    ManagerDB.updateOffset(subscriber.getUrl(), offset);

                } else {
                    Log4jUtility.getLogger().error("FAILED : " + subscriber.getUrl() + " (status: " + statusCode + ")");
                    if (AppConfig.getRetryCount() != 0 || !mode.equals("limited")) {
                        ManagerDB.insertToFailedMessages(subscriber.getUrl(), message, offset, topic);
                        Log4jUtility.getLogger().error("Inserted to failed message with url: " + subscriber.getUrl() + " and offset: " + offset);

                    }

                    if (mode.equals("unlimited")) {
                        ManagerDB.deleteFromSubscribers(subscriber.getUrl(), topic);
                    }
                }
            }

            lastOffset = offset;

            OffsetConfig.updateMainOffset(topic, lastOffset);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getLastOffset() {
        return lastOffset;
    }

    public String getTopic() {
        return topic;
    }
}