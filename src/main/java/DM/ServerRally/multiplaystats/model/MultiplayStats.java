package DM.ServerRally.multiplaystats.model;


import DM.ServerRally.user.model.User;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "multiplay_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class MultiplayStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer wins;

    @Column(name = "best_time")
    private Double bestTime;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
