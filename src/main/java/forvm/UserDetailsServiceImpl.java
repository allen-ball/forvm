/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package forvm;

import forvm.entity.Author;
import forvm.entity.Credential;
import forvm.entity.Subscriber;
import forvm.repository.AuthorRepository;
import forvm.repository.CredentialRepository;
import forvm.repository.SubscriberRepository;
import java.util.HashSet;
import java.util.Optional;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link UserDetailsService} implementation
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Service
@NoArgsConstructor @ToString @Log4j2
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired private CredentialRepository credentialRepository;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private SubscriberRepository subscriberRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = null;
        Optional<Credential> credential =
            credentialRepository.findById(username);

        if (credential.isPresent()) {
            HashSet<GrantedAuthority> set = new HashSet<>();

            subscriberRepository.findById(username)
                .ifPresent(t -> set.add(new SimpleGrantedAuthority("SUBSCRIBER")));

            authorRepository.findById(username)
                .ifPresent(t -> set.add(new SimpleGrantedAuthority("AUTHOR")));

            user = new User(username, credential.get().getPassword(), set);
        } else {
            throw new UsernameNotFoundException(username);
        }

        return user;
    }
}
