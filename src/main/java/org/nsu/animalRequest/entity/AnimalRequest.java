package org.nsu.animalRequest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    private User initiator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recipient", nullable = false)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_initiator_animal_card")
    private AnimalCard initiatorAnimalCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_recipient_animal_card", nullable = false)
    private AnimalCard recipientAnimalCard;

    @Column(name = "request_date", nullable = false)
    private Date requestDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_goals", nullable = false)
    private PlacementGoal goals;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_status", nullable = false)
    private AnimalRequestStatus status;

}
