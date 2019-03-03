/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package forvm;

import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@NoArgsConstructor(access = PRIVATE)
public abstract class WebSecurityConfigurerImpl
                      extends WebSecurityConfigurerAdapter {
    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired private UserDetailsService userDetailsService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder);
    }

    @Override
    public String toString() { return super.toString(); }

    /**
     * {@link org.springframework.security.config.annotation.web.WebSecurityConfigurer}
     * implementation for the API
     */
    @Configuration
    @Order(1)
    public static class API extends WebSecurityConfigurerImpl {

        /**
         * Sole constructor.
         */
        public API() { super(); }

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
    public static class UI extends WebSecurityConfigurerImpl {

        /**
         * Sole constructor.
         */
        public UI() { super(); }

        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring()
                .antMatchers("/css/**", "/js/**", "/images/**");
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
