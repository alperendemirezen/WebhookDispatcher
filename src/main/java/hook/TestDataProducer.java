package hook;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class TestDataProducer {
    private static final String[] cities = {"Istanbul", "Ankara", "Izmir", "Bursa", "Antalya"};
    private static final String[] statuses = {"dolu", "boş", "bakımda"};
    private static final Random random = new Random();

    public static void main(String[] args) throws Exception {
        System.out.println("📦 SimpleKafkaTestProducer başladı. 20 veri gönderiliyor...");

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        ObjectMapper mapper = new ObjectMapper();

        String topic = "test-webhook-data";

        for (int i = 0; i < 100; i++) {
            Map<String, Object> record = new HashMap<>();
            record.put("id", i);
            record.put("city", cities[i % cities.length]);
            record.put("status", statuses[random.nextInt(statuses.length)]);
            record.put("speed", 30 + random.nextInt(70));
            record.put("routeNumber", 1 + random.nextInt(50));
            record.put("timestamp", "20250710" + String.format("%06d", i));

            String json = mapper.writeValueAsString(record);
            ProducerRecord<String, String> message = new ProducerRecord<>(topic, json);
            producer.send(message);

            System.out.println("➡️ Gönderildi: " + json);
        }

        producer.flush();
        producer.close();
        System.out.println("🎉 Veri gönderimi tamamlandı.");
    }
}
