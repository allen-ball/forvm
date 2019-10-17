/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package forvm;

import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static lombok.AccessLevel.PRIVATE;

/**
 * {@link org.springframework.security.config.annotation.web.WebSecurityConfigurer}
 * abstract base class
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@NoArgsConstructor(access = PRIVATE) @Log4j2
public abstract class WebSecurityConfigurerImpl
                      extends WebSecurityConfigurerAdapter {
    @Autowired private UserDetailsService userDetailsService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder);
    }

    /**
     * {@link org.springframework.security.config.annotation.web.WebSecurityConfigurer}
     * implementation for the API
     */
    @Configuration
    @Order(1)
    @NoArgsConstructor @ToString
    public static class API extends WebSecurityConfigurerImpl {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/api/**")
                .csrf().disable()
                .authorizeRequests().anyRequest().authenticated()
                .and().httpBasic();
        }
    }

    /**
     * {@link org.springframework.security.config.annotation.web.WebSecurityConfigurer}
     * implementation for the UI
     */
    @Configuration
    @Order(2)
    @NoArgsConstructor @ToString
    public static class UI extends WebSecurityConfigurerImpl {
        @Override
        public void configure(WebSecurity web) {
            web.ignoring()
                .antMatchers("/css/**", "/js/**", "/images/**",
                             "/webjars/**", "/webjarsjs");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .authorizeRequests().anyRequest().permitAll()
                .and().formLogin().loginPage("/login").permitAll()
                .and().logout().permitAll();
        }
    }
}
