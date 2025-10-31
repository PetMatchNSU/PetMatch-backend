package org.nsu.authorization.core.repositories;

import org.nsu.users.entity.userAuthority.UserAuthority;
import org.nsu.users.entity.userAuthority.UserAuthorityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthorityRepository extends JpaRepository<UserAuthority, UserAuthorityId> {

}