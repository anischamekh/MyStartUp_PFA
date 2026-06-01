package tn.iteam.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "team_snapshots")
public class TeamSnapshot {

    @Id
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "team_leader_id")
    private Long teamLeaderUserId;
}
