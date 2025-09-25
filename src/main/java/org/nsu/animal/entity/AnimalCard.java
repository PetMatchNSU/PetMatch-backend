package org.nsu.animal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.nsu.users.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_animals_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnimalCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_animal", nullable = false)
    @ToString.Exclude
    private Animal animal;

    @Column(name = "breed", length = 64)
    private String breed;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 1, columnDefinition = "VARCHAR(1)")
    private AnimalGender gender;

    @Column(name = "birthdate", nullable = false)
    private LocalDate birthdate;

    @Column(name = "weight", precision = 6, scale = 1)
    private BigDecimal weight;

    @Column(name = "color", nullable = false, length = 200)
    private String color;

    @Column(name = "genetic_diseases", nullable = false, length = 2000)
    private String geneticDiseases;

    @Column(name = "description", length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_goal", nullable = false)
    @ToString.Exclude
    private PlacementGoal goal;

    @Column(name = "cost", precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @Column(name = "updated", nullable = false)
    private LocalDateTime updated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_status")
    @ToString.Exclude
    private AnimalCardStatus status;
}
