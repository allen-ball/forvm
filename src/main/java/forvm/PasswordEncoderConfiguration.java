/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm;

import ball.spring.MD5CryptPasswordEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * {@link PasswordEncoder} {@link Configuration}
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@Configuration
public class PasswordEncoderConfiguration {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Sole constructor.
     */
    public PasswordEncoderConfiguration () { }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new MD5CryptPasswordEncoder();
    }

    @Override
    public String toString() { return super.toString(); }
}
