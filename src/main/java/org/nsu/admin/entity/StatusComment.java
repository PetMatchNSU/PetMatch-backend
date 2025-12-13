package org.nsu.admin.entity;


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
import org.nsu.animal.entity.AnimalCardStatus;
import org.nsu.users.entity.Status;
import org.nsu.users.entity.User;

import java.sql.Timestamp;

@Entity
@Table(name = "t_status_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_status")
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_animal_card_status")
    private AnimalCardStatus animalCardStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_animal_card")
    private AnimalCard animalCard;

    @Column(name = "comment", length = 1024)
    private String comment;

    @Column(name = "date", nullable = false)
    private Timestamp date;

}
