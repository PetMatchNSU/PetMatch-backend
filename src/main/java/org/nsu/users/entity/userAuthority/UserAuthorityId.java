package org.nsu.users.entity.userAuthority;

import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class UserAuthorityId implements Serializable {

    private Long user;

    private Long authority;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserAuthorityId that = (UserAuthorityId) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(authority, that.authority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, authority);
    }
}