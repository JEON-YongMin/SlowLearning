package greensnail_backend.GreenSnail.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "article")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String content;

    @Lob
    private String aiSummary;

    @Enumerated(EnumType.STRING)
    private Level level;
}
