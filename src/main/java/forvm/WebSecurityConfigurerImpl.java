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
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static lombok.AccessLevel.PRIVATE;

/**
 * {@link org.springframework.security.config.annotation.web.WebSecurityConfigurer}
 * abstract base class.
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@NoArgsConstructor(access = PRIVATE) @Log4j2
public abstract class WebSecurityConfigurerImpl extends WebSecurityConfigurerAdapter {
    @Autowired private UserDetailsService userDetailsService = null;
    @Autowired private PasswordEncoder passwordEncoder = null;

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder);
    }

    /**
     * {@link org.springframework.security.config.annotation.web.WebSecurityConfigurer}
     * implementation for the API.
     */
    @Configuration
    @Order(1)
    @NoArgsConstructor @ToString
    public static class API extends WebSecurityConfigurerImpl {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/api/**")
                .authorizeRequests(t -> t.anyRequest().authenticated())
                .csrf(t -> t.disable())
                .httpBasic(t -> t.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.FORBIDDEN)));
        }
    }

    /**
     * {@link org.springframework.security.config.annotation.web.WebSecurityConfigurer}
     * implementation for the UI.
     */
    @Configuration
    @Order(2)
    @NoArgsConstructor @ToString
    public static class UI extends WebSecurityConfigurerImpl {
        private static final String[] IGNORE = {
            "/css/**", "/js/**", "/images/**", "/webjars/**", "/webjarsjs"
        };

        @Autowired private OidcUserService oidcUserService = null;

        @Override
        public void configure(WebSecurity web) {
            web.ignoring().antMatchers(IGNORE);
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/**")
                .authorizeRequests(t -> t.anyRequest().permitAll())
                .formLogin(t -> t.loginPage("/login").permitAll())
                .logout(t -> t.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                              .logoutSuccessUrl("/").permitAll());

            try {
                var repository =
                    getApplicationContext().getBean(ClientRegistrationRepository.class);

                if (repository != null) {
                    http.oauth2Login(t -> t.clientRegistrationRepository(repository)
                                           .userInfoEndpoint(u -> u.oidcUserService(oidcUserService))
                                           .loginPage("/login").permitAll());
                }
            } catch (Exception exception) {
            }
        }
    }
}
