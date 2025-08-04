package hook;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import log.Log4jUtility;

import java.time.Duration;
import java.util.Collections;

//This class just for unlimited mode!!
public class PrivateWorker implements Runnable {

    private Subscriber subscriber;

    private KafkaConsumer<String, String> consumer;

    private volatile boolean running = true;

    public PrivateWorker(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void run() {

        try {
            Log4jUtility.getLogger().debug("Started on thread: " + Thread.currentThread().getName());
            ThreadStatusManager.registerThread();

            consumer = new KafkaConsumer<>(KafkaProperties.getKafkaProperties());
            TopicPartition partition = new TopicPartition(subscriber.getTopic(), 0);
            consumer.assign(Collections.singletonList(partition));

            consumer.seekToBeginning(Collections.singletonList(partition));
            long beginningOffset = consumer.position(partition);

            if (subscriber.getOffset() + 1 >= beginningOffset) {
                consumer.seek(partition, subscriber.getOffset() + 1);
            } else {
                consumer.seek(partition, beginningOffset);
            }

            while (running) {

                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(5000));

                if (records.isEmpty()) {
                    if (records.isEmpty()) Log4jUtility.getLogger().debug("Records is empty");

                    consumer.seekToBeginning(Collections.singletonList(partition));
                    beginningOffset = consumer.position(partition);

                    if (subscriber.getOffset() + 1 >= beginningOffset) {
                        consumer.seek(partition, subscriber.getOffset() + 1);
                    } else {
                        consumer.seek(partition, beginningOffset);
                    }
                    PauseController.waitIfPaused();
                    continue;
                }
                Log4jUtility.getLogger().debug("POLLED PRIVATE" + records.count());

                for (ConsumerRecord<String, String> record : records) {
                    if (!Thread.currentThread().isInterrupted()) {
                        Log4jUtility.getLogger().debug("PRIVATE: Offset:" + record.offset() + "| New message received: " + record.value());
                        forwardToWebhooks(record.value(), record.offset());
                        PauseController.waitIfPaused();
                        if (running == false) break;
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

            int statusCode;

            statusCode = WebhookSender.send(subscriber.getUrl(), message, offset);
            if (statusCode == 200) {
                Log4jUtility.getLogger().debug("SUCCESS PRIVATE : " + subscriber.getUrl() + " (status: " + statusCode + ")");
                ManagerDB.privateUpdateOffset(subscriber.getUrl(), offset);
                subscriber.setOffset(offset);
            } else {
                Log4jUtility.getLogger().error("FAILED : " + subscriber.getUrl() + " (status: " + statusCode + ")");
                ManagerDB.insertToFailedMessages(subscriber.getUrl(), message, offset, subscriber.getTopic());
                running = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public Subscriber getSubscriber() {
        return subscriber;
    }

    public void stop() {
        running = false;
    }


}
