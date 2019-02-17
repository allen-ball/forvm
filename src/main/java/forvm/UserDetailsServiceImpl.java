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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * <p>{@injected.fields}</p>
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired private CredentialRepository credentialRepository;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private SubscriberRepository subscriberRepository;

    /**
     * Sole constructor.
     */
    public UserDetailsServiceImpl() { }

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

    @Override
    public String toString() { return super.toString(); }
}
