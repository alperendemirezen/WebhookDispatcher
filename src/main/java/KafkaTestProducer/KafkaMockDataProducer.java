package KafkaTestProducer;

import org.apache.kafka.clients.producer.*;

import java.time.LocalDateTime;
import java.util.Properties;

public class KafkaMockDataProducer {

    public static void main(String[] args) throws Exception {
        // Kafka ayarları
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        String topic = "anomaly-detection";

        // Opsiyonel: anahtar olarak card_no kullanmak isterseniz true yapın
        boolean useKeyCardNo = false;

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            long total = 10_000; // üretilecek kayıt sayısı (isterseniz değiştirin)
            LocalDateTime baseTime = LocalDateTime.now();

            for (long i = 0; i < total; i++) {
                String json = GenerateMockData.generate(i, baseTime);

                String key = null;
                if (useKeyCardNo) {
                    // Key olarak card_no
                    // (Basitçe JSON içinden çekmek yerine GenerateMockData isterseniz key de döndürebilir;
                    // burada hızlı bir çözüm olarak çok küçük bir parse yapılabilir.)
                    int k = json.indexOf("\"card_no\":\"");
                    if (k > 0) {
                        int start = k + "\"card_no\":\"".length();
                        int end = json.indexOf("\"", start);
                        if (end > start) key = json.substring(start, end);
                    }
                }

                ProducerRecord<String, String> rec = new ProducerRecord<>(topic, key, json);
                producer.send(rec);

                if (i > 0 && i % 1000 == 0) {
                    System.out.printf("✔ %d mesaj gönderildi.%n", i);
                }
            }

            producer.flush();
        }

        System.out.println("✅ Tüm veriler Kafka'ya gönderildi.");
    }
}
