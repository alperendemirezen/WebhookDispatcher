package KafkaTestProducer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class KafkaAnomalyDataProducer {

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        String topic = "test-transaction-anomaly";


        String customerFlag = "1";
        String routeCode = "30030";
        String usageAmt = "00003000";



        for (int i = 0; i < 100; i++) {
            Map<String, Object> record = new HashMap<>();
            String timestamp = LocalDateTime.now().plusSeconds(i).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            record.put("type", "T");
            record.put("record_id", "D");
            record.put("travel_seq_no", String.format("%08d", 90000000 + i));
            record.put("trans_seq_no", String.format("%08d", 90000000 + i + 1));
            record.put("sam_id", "05152276");
            record.put("validator_id", "5440066");
            record.put("bus_id", "28100");
            record.put("route_code", routeCode);
            record.put("customer_flag", customerFlag);
            record.put("usage_amt", usageAmt);
            record.put("boarding_date_time", timestamp);
            record.put("start_date_time", timestamp);
            record.put("card_no", "0282400000999" + i);

            String json = new ObjectMapper().writeValueAsString(record);
            producer.send(new ProducerRecord<>(topic, null, json));

            if (i > 0 && i % 100 == 0) {
                System.out.printf("🚨 %d anomali verisi gönderildi.%n", i);
            }

            Thread.sleep(10); // küçük bekleme ile gerçekçi test
        }

        producer.flush();
        producer.close();
        System.out.println("✅ Anomali test verileri gönderildi.");
    }
}
