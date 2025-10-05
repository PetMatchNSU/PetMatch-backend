package org.nsu.animalRequest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.animal.entity.PlacementGoal;
import org.nsu.users.entity.User;

import java.sql.Date;

@Entity
@Table(name = "t_animals_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnimalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_initiator", nullable = false)
    @ToString.Exclude
    private User initiator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recipient", nullable = false)
    @ToString.Exclude
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_initiator_animal_card")
    @ToString.Exclude
    private AnimalCard initiatorAnimalCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recipient_animal_card", nullable = false)
    @ToString.Exclude
    private AnimalCard recipientAnimalCard;

    @Column(name = "request_date", nullable = false)
    private Date requestDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_goals", nullable = false)
    @ToString.Exclude
    private PlacementGoal goals;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_status", nullable = false)
    @ToString.Exclude
    private AnimalRequestStatus status;

}
