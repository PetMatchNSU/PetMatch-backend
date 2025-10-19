package org.nsu.users.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "t_bond_times")
@Getter
@Setter
@NoArgsConstructor
public class BondTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Время начала доступности
    @Column(name = "start", nullable = false)
    private LocalTime start;

    // Время конца доступности
    @Column(name = "end", nullable = false)
    private LocalTime end;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;
}