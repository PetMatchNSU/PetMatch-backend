package org.nsu.authorization.core.services;

import lombok.RequiredArgsConstructor;
import org.nsu.authorization.core.exceptions.authorization.PersonNotFoundException;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.authorization.core.security.PersonDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public PersonDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return loadUserByEmail(email);
    }

    public PersonDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        return new PersonDetails(userRepository
                .findByEmail(email)
                .orElseThrow(
                        () -> new PersonNotFoundException("User with this email not found")
                )
        );
    }

    public PersonDetails loadUserById(long id) throws UsernameNotFoundException {
        return new PersonDetails(userRepository
                .findById(id)
                .orElseThrow(
                        () -> new PersonNotFoundException("User with this id not found"))
        );
    }

}