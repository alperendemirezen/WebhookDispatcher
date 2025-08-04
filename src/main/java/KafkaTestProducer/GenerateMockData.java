package KafkaTestProducer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GenerateMockData {
    private static final SecureRandom rand = new SecureRandom();

    // Zorunlu alanların dayandığı mevcut kurallar
    private static final String[] CUSTOMER_FLAGS = {"0", "1", "2", "3"}; // 0: free, 1: regular, 2: student, 3: retired (örnek)
    private static final String[] ROUTE_CODES = {
            "30000","30001","30002","30003","30004","30005","30006","30007","30008","30009",
            "30120","30121","30122"
    };

    // Çift/tek route_code kuralına göre usage_amt haritaları (aynen korundu)
    private static final Map<String, String> usageAmtEven = new HashMap<>();
    private static final Map<String, String> usageAmtOdd  = new HashMap<>();
    static {
        usageAmtEven.put("0", "00000000");
        usageAmtEven.put("1", "00000500");
        usageAmtEven.put("2", "00000700");
        usageAmtEven.put("3", "00001300");

        usageAmtOdd.put("0", "00000000");
        usageAmtOdd.put("1", "00000600");
        usageAmtOdd.put("2", "00000900");
        usageAmtOdd.put("3", "00001500");
    }

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static String leftPadNumber(long n, int width) {
        return String.format("%0" + width + "d", n);
    }

    private static String randomFrom(String[] arr) {
        return arr[rand.nextInt(arr.length)];
    }

    private static String randomHex(int len) {
        char[] hex = "0123456789abcdef".toCharArray();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(hex[rand.nextInt(hex.length)]);
        return sb.toString();
    }

    private static String pickUsageAmt(String routeCode, String customerFlag) {
        boolean isEven = Integer.parseInt(routeCode) % 2 == 0;
        return isEven ? usageAmtEven.get(customerFlag) : usageAmtOdd.get(customerFlag);
    }

    private static String pickCustomerCnt() {
        // ~%80 "001", %20 "002-004"
        int r = rand.nextInt(100);
        if (r < 80) return "001";
        int more = 2 + rand.nextInt(3); // 2..4
        return leftPadNumber(more, 3);
    }

    private static String maybeTapId() {
        // %50 boş, %50 dolu (12-16 haneli hex)
        if (rand.nextBoolean()) return "";
        int len = 12 + rand.nextInt(5); // 12..16
        return randomHex(len);
    }

    private static String pickRider() {
        // 0/1 (örnek): çoğunlukla 0
        return rand.nextInt(100) < 85 ? "0" : "1";
    }

    private static String pickTariffNumber() {
        // Basit örnek: 0..3 arası
        return String.valueOf(rand.nextInt(4));
    }

    private static String randomBusId() {
        return String.valueOf(28000 + rand.nextInt(1000)); // 28000-28999
    }

    private static String randomSamId() {
        return String.valueOf(5000000 + rand.nextInt(999999)); // 5000000-5999999 (örnek)
    }

    private static String randomValidatorId() {
        return String.valueOf(5400000 + rand.nextInt(999999)); // 5400000-6399999 (örnek)
    }

    private static String randomCardNo() {
        // 16 hane: "0282400000400" + 4 hane
        return "0282400000400" + leftPadNumber(rand.nextInt(10000), 4);
    }

    public static String generate(long index, LocalDateTime baseTime) throws Exception {
        Map<String, Object> record = new LinkedHashMap<>();

        // --- Zorunlu alanlara bağlı seçimler ---
        String routeCode     = randomFrom(ROUTE_CODES);
        String customerFlag  = randomFrom(CUSTOMER_FLAGS);
        String usageAmt      = pickUsageAmt(routeCode, customerFlag);
        String cardNo        = randomCardNo();
        String customerCnt   = pickCustomerCnt();
        String tapId         = maybeTapId();
        String rider         = pickRider();
        String tariffNumber  = pickTariffNumber();
        String transFlag     = rand.nextInt(100) < 90 ? "1" : "0"; // çoğunlukla 1
        String samSeqNo      = String.valueOf(1 + (index % 999999)); // 1..999999

        // Zamanlar
        LocalDateTime boardingTs = baseTime.plusSeconds(index);
        LocalDateTime startTs    = boardingTs.minusMinutes(rand.nextInt(20)); // örnek

        // --- Diğer alanlar (eski mantığa uygun) ---
        String travelSeqNo = leftPadNumber(index, 8);
        String transSeqNo  = leftPadNumber(index + 1, 8);
        String samId       = randomSamId();
        String validatorId = randomValidatorId();
        String busId       = randomBusId();

        // --- Kayıt objesi ---
        record.put("type", "T");
        record.put("record_id", "D");
        record.put("travel_seq_no", travelSeqNo);
        record.put("trans_seq_no", transSeqNo);
        record.put("sam_id", samId);
        record.put("validator_id", validatorId);
        record.put("bus_id", busId);

        // Zorunlu alanlar (senin listene göre)
        record.put("route_code", routeCode);
        record.put("customer_flag", customerFlag);
        record.put("usage_amt", usageAmt);
        record.put("card_no", cardNo);
        record.put("boarding_date_time", boardingTs.format(TS_FMT));
        record.put("sam_seq_no", samSeqNo);
        record.put("trans_flag", transFlag);
        record.put("tap_id", tapId);
        record.put("customer_cnt", customerCnt);
        record.put("rider", rider);
        record.put("tariff_number", tariffNumber);

        // Ek (opsiyonel) alanlar – örnek
        record.put("start_date_time", startTs.format(TS_FMT));
        record.put("data_save_flag", rand.nextBoolean() ? "0" : "1");
        record.put("usage_cnt", leftPadNumber(rand.nextInt(200) + 1, 8)); // örnek sayaç
        record.put("station_type", String.valueOf(1 + rand.nextInt(8))); // 1..8
        record.put("transmit_cnt", String.valueOf(1 + rand.nextInt(3))); // 1..3

        return new ObjectMapper().writeValueAsString(record);
    }
}
