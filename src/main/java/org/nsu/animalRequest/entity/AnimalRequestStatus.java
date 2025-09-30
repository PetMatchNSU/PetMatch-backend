package org.nsu.animalRequest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_animals_requests_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnimalRequestStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "status", nullable = false, length = 32, columnDefinition = "VARCHAR(32)")
    private String status;

}
