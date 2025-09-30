package org.nsu.admin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.nsu.users.entity.Status;
import org.nsu.users.entity.User;

import java.util.Date;

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
    @JoinColumn(name = "id_status", nullable = false)
    @ToString.Exclude
    private Status idStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false)
    @ToString.Exclude
    private User idUser;

    @Column(name = "comment", length = 1024)
    private String comment;

    @Column(name = "date", nullable = false)
    private Date date;

}
