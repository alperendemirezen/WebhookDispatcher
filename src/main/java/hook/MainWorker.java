package hook;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.*;

public class MainWorker implements Runnable {

    private KafkaConsumer<String, String> consumer;
    private List<Subscriber> subscribers = new ArrayList<>();
    private String mode;


    @Override
    public void run() {
        try {
            mode = AppConfig.getRetryMode();
            System.out.println("Mode: " + AppConfig.getRetryMode());
            System.out.println("[MainWorker] Started on thread: " + Thread.currentThread().getName());
            ThreadStatusManager.registerThread();

            consumer = new KafkaConsumer<>(KafkaProperties.getKafkaProperties());
            TopicPartition partition = new TopicPartition(KafkaProperties.topic, 0);
            consumer.assign(Collections.singletonList(partition));
            consumer.seekToBeginning(Collections.singletonList(partition));
            long beginningOffset = consumer.position(partition);

            long startOffset = (AppConfig.getConfigStartOffset() != -1) ? AppConfig.getConfigStartOffset() : AppConfig.getMainLastOffset();
            if (startOffset < beginningOffset) {
                System.out.println("Start offset is too early. Starting from beginning offset: " + beginningOffset);
                consumer.seek(partition, beginningOffset);
            } else {
                consumer.seek(partition, startOffset);
            }

            while (true) {

                ManagerDB.getUrlList(subscribers);
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(5000));

                if (records.isEmpty()) {
                    if (records.isEmpty()) System.out.println("Records is empty");
                    PauseController.waitIfPaused();
                    continue;
                }

                System.out.println("POLLED: " + records.count());

                for (ConsumerRecord<String, String> record : records) {
                    if (!Thread.currentThread().isInterrupted()) {
                        System.out.println("MAIN : Offset:" + record.offset() + "| New message received: " + record.value());
                        forwardToWebhooks(record.value(), record.offset());
                        PauseController.waitIfPaused();
                        if (mode.equals("unlimited")) {
                            ManagerDB.getUrlList(subscribers);
                        }
                    }
                }
                consumer.commitSync();
            }
        } catch (Exception e) {
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
                    System.out.println(" Skipped: " + subscriber.getUrl() + " (offset=" + offset + " <= last_offset=" + subscriber.getOffset() + ")");
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
                            System.out.println("Retrying (" + attempts + "): " + subscriber.getUrl());
                        }
                    }
                }

                if (sent) {
                    System.out.println("SUCCESS : " + subscriber.getUrl() + " (status: " + statusCode + ")");
                    ManagerDB.updateOffset(subscriber.getUrl(), offset);

                } else {
                    System.err.println("FAILED : " + subscriber.getUrl() + " (status: " + statusCode + ")");
                    ManagerDB.insertToFailedMessages(subscriber.getUrl(), message, offset);
                    System.out.println("Inserted to failed message with url: " + subscriber.getUrl() + " and offset: " + offset);

                    if (mode.equals("unlimited")) {
                        ManagerDB.deleteFromSubscribers(subscriber.getUrl());
                    }
                }
            }

            AppConfig.setMainLastOffset(offset);
            AppConfig.setStartOffset(offset);
            AppConfig.saveConfig();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}