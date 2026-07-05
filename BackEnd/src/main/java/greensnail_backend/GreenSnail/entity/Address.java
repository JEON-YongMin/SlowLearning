package greensnail_backend.GreenSnail.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String zipcode;     // 우편번호
    private String address;     // 기본 주소
    private String addressDetail; // 상세 주소
}