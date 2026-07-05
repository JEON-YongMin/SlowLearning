package greensnail_backend.GreenSnail.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scenario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 이게 시나리오 id

    private String title; // 제목

    private String situation; // 상황 설명

    @ElementCollection
    @CollectionTable(name = "scenario_missions", joinColumns = @JoinColumn(name = "scenario_id"))
    @MapKeyColumn(name = "mission_name")
    @Column(name = "completed")
    private Map<String, Boolean> missions; // 미션 목록 (미션이름 + 완료여부)

    @OneToMany(mappedBy = "scenario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Dialogue> dialogue; // 대화 목록
}
