package org.nsu.users.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "t_authorities")
public class Authority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false,  length = 64)
    private String name;

    @Column(unique = true, nullable = false,  length = 256)
    private String description;
}
