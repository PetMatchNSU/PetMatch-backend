package org.nsu.users.entity.userAuthority;

import org.nsu.users.entity.Authority;
import org.nsu.users.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_users_authorities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserAuthorityId.class)
public class UserAuthority {
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_authority", nullable = false)
    private Authority authority;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_user", nullable = false)
    private User user;
}
