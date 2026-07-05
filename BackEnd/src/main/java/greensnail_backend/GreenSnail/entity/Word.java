package greensnail_backend.GreenSnail.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String word;

    @Column(nullable = false)
    private String easyDefinition;

    @Column(nullable = false)
    private String example;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String dictionaryDefinition;
}
