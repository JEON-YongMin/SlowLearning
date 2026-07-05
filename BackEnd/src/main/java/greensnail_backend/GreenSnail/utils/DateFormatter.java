package greensnail_backend.GreenSnail.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateFormatter {

    // 날짜 형식을 맞추는 메서드
    public static String formatDate(String dateStr) {
        // 8자리 숫자인지 확인 (yyyyMMdd 형식)
        if (dateStr.length() == 8 && dateStr.matches("\\d{8}")) {
            // yyyyMMdd -> yyyy-MM-dd 형식으로 변환
            try {
                LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
                return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception e) {
                throw new IllegalArgumentException("잘못된 날짜 형식입니다.");
            }
        } else if (dateStr.length() == 10 && dateStr.contains("-")) {
            // 이미 yyyy-MM-dd 형식 = 그대로 반환
            return dateStr;
        } else {
            throw new IllegalArgumentException("날짜 형식은 yyyyMMdd 또는 yyyy-MM-dd으로 작성해주세요.");
        }
    }
}
