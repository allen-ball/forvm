package forvm;
/*-
 * ##########################################################################
 * forvm Blog Publishing Platform
 * %%
 * Copyright (C) 2018 - 2021 Allen D. Ball
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
import forvm.repository.AuthorRepository;
import forvm.repository.CredentialRepository;
import forvm.repository.SubscriberRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;

/**
 * User details {@link Configuration}.
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 */
@Configuration
@NoArgsConstructor @ToString @Log4j2
public class UserServicesConfiguration {
    @Autowired private CredentialRepository credentialRepository = null;
    @Autowired private AuthorRepository authorRepository = null;
    @Autowired private SubscriberRepository subscriberRepository = null;


    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest,OAuth2User> oAuth2UserService() {
        return new OAuth2UserServiceImpl();
    }

    @Bean
    public OidcUserService oidcUserService() {
        return new OidcUserServiceImpl();
    }

    private Set<GrantedAuthority> getGrantedAuthoritySet(String username) {
        var set = new HashSet<GrantedAuthority>();

        subscriberRepository.findById(username)
            .ifPresent(t -> set.add(new SimpleGrantedAuthority("SUBSCRIBER")));

        authorRepository.findById(username)
            .ifPresent(t -> set.add(new SimpleGrantedAuthority("AUTHOR")));

        return set;
    }

    @NoArgsConstructor @ToString
    private class UserDetailsServiceImpl implements UserDetailsService {
        @Override
        @Transactional(readOnly = true)
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            User user = null;

            try {
                var credential = credentialRepository.findById(username);

                user = new
                    User(username,
                         credential.get().getPassword(),
                         getGrantedAuthoritySet(username));
            } catch (UsernameNotFoundException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new UsernameNotFoundException(username);
            }

            return user;
        }
    }

    @NoArgsConstructor @ToString
    private class OAuth2UserServiceImpl extends DefaultOAuth2UserService {
        private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

        @Override
        @Transactional(readOnly = true)
        public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
            var attribute =
                request.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();
            var user = delegate.loadUser(request);

            try {
                user =
                    new DefaultOAuth2User(getGrantedAuthoritySet(user.getName()),
                                          user.getAttributes(), attribute);
            } catch (OAuth2AuthenticationException exception) {
                throw exception;
            } catch (Exception exception) {
                log.warn("{}", request, exception);
            }

            return user;
        }
    }

    @NoArgsConstructor @ToString
    private class OidcUserServiceImpl extends OidcUserService {
        { setOauth2UserService(oAuth2UserService()); }

        @Override
        @Transactional(readOnly = true)
        public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {
            var attribute =
                request.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();
            var user = super.loadUser(request);

            try {
                user =
                    new DefaultOidcUser(getGrantedAuthoritySet(user.getName()),
                                        user.getIdToken(), user.getUserInfo(),
                                        attribute);
            } catch (OAuth2AuthenticationException exception) {
                throw exception;
            } catch (Exception exception) {
                log.warn("{}", request, exception);
            }

            return user;
        }
    }
}
