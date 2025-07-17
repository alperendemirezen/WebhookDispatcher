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
        RetryWorker.privateWorkers.add(this);

        try {
            System.out.println("[PrivateWorker] Started on thread: " + Thread.currentThread().getName());
            ThreadStatusManager.registerThread();

            consumer = new KafkaConsumer<>(KafkaProperties.getKafkaProperties());
            TopicPartition partition = new TopicPartition(KafkaProperties.topic, 0);
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
                    if (records.isEmpty()) System.out.println("Records is empty");
                    PauseController.waitIfPaused();
                    continue;
                }
                System.out.println("POLLED PRIVATE" + records.count());

                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("PRIVATE: Offset:" + record.offset() + "| New message received: " + record.value());
                    forwardToWebhooks(record.value(), record.offset());
                    PauseController.waitIfPaused();
                    if (running == false) break;
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
                subscriber.setOffset(offset);
            } else {
                System.out.println("FAILED : " + subscriber.getUrl() + " (status: " + statusCode + ")");
                ManagerDB.insertToFailedMessages(subscriber.getUrl(), message, offset);
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
