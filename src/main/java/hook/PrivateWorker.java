package hook;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

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
            System.out.println("[PrivateWorker] Started on thread: " + Thread.currentThread().getName());
            ThreadStatusManager.registerThread();

            consumer = new KafkaConsumer<>(KafkaProperties.getKafkaProperties());
            TopicPartition partition = new TopicPartition(KafkaProperties.topic, 0);
            consumer.assign(Collections.singletonList(partition));
            consumer.seek(partition, subscriber.getOffset() + 1);

            while (running) {

                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(5000));
                if (records.isEmpty()) continue;

                System.out.println("POLLED PRIVATE" + records.count());

                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("Offset:" + record.offset() + "| New message received: " + record.value());
                    forwardToWebhooks(record.value(), record.offset());
                    PauseController.waitIfPaused();
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

            int statusCode = -1;

            statusCode = WebhookSender.send(subscriber.getUrl(), message);
            if (statusCode == 200) {
                System.out.println("SUCCESS PRIVATE : " + subscriber.getUrl() + " (status: " + statusCode + ")");
                ManagerDB.privateUpdateOffset(subscriber.getUrl(), offset);
            } else {
                System.out.println("FAILED : " + subscriber.getUrl() + " (status: " + statusCode + ")");
                ManagerDB.insertToFailedMessages(subscriber.getUrl(), message, offset);
                running = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
