/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link Tika} {@link Configuration}
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@Configuration
public class TikaConfiguration {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Sole constructor.
     */
    public TikaConfiguration () { }

    @Bean
    public Tika tika() { return new Tika(); }

    @Override
    public String toString() { return super.toString(); }
}
