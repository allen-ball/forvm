package forvm;
/*-
 * ##########################################################################
 * forvm Blog Publishing Platform
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2018 - 2020 Allen D. Ball
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ##########################################################################
 */
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
 * {@link UserDetailsService} implementation.
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Service
@NoArgsConstructor @ToString @Log4j2
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired private CredentialRepository credentialRepository = null;
    @Autowired private AuthorRepository authorRepository = null;
    @Autowired private SubscriberRepository subscriberRepository = null;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = null;

        try {
            Optional<Credential> credential =
                credentialRepository.findById(username);
            HashSet<GrantedAuthority> set = new HashSet<>();

            subscriberRepository.findById(username)
                .ifPresent(t -> set.add(new SimpleGrantedAuthority("SUBSCRIBER")));

            authorRepository.findById(username)
                .ifPresent(t -> set.add(new SimpleGrantedAuthority("AUTHOR")));

            user = new User(username, credential.get().getPassword(), set);
        } catch (UsernameNotFoundException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new UsernameNotFoundException(username);
        }

        return user;
    }
}
