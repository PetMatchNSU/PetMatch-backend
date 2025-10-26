package org.nsu.users.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "t_bond_times")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BondTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start", nullable = false)
    private LocalTime startContactTime;

    @Column(name = "end", nullable = false)
    private LocalTime endContactTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", insertable = false, updatable = false)
    private User user;

    public void setStart(@NotNull LocalTime bondTimeStart) {
        startContactTime = bondTimeStart;
    }

    public void setEnd(@NotNull LocalTime bondTimeEnd) {
        endContactTime = bondTimeEnd;
    }
}
