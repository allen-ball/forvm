/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package forvm;

import ball.spring.MD5CryptPasswordEncoder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * {@link PasswordEncoder} {@link Configuration}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Configuration
@NoArgsConstructor @ToString
public class PasswordEncoderConfiguration {
    private static final Logger LOGGER = LogManager.getLogger();

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new MD5CryptPasswordEncoder();
    }
}
