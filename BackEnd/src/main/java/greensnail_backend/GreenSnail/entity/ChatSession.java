package greensnail_backend.GreenSnail.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String sessionJson;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
