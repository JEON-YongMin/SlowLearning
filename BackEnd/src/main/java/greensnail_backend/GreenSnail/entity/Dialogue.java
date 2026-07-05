package greensnail_backend.GreenSnail.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dialogue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String role;
    private String message;

    @ManyToOne
    @JoinColumn(name = "scenario_id")
    private Scenario scenario;}