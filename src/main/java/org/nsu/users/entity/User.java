package org.nsu.users.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Entity
@Table(name = "t_users")
@FieldNameConstants
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "first_name", nullable = false, length = 64)
    private String firstName;

    @Column(name = "second_name", nullable = false, length = 64)
    private String secondName;

    @Column(name = "middle_name", length = 64)
    private String middleName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_status", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 1, columnDefinition = "VARCHAR(1)")
    private Gender gender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_region", nullable = false)
    private Region region;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "t_users_authorities", joinColumns = @JoinColumn(name = "id_user", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "id_authority", referencedColumnName = "id"))
    private Set<Authority> authorities = new HashSet<>(); // JWUtils are dependent on it

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<BondTime> bondTimes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Contact> contacts = new ArrayList<>();

    @Column(name = "is_email_verified")
    private boolean isEmailVerified = false;

}
