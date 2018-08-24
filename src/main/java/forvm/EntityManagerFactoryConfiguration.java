/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.jpa.EntityManagerFactoryDependsOnPostProcessor;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EntityManagerFactoryDependsOnPostProcessor} {@link Configuration}
 * implementation.  See
 * {@link.uri https://docs.spring.io/spring-boot/docs/current/reference/html/howto-data-access.html#howto-configure-a-component-that-is-used-by-JPA target=newtab 80.12 Configure a Component that is Used by JPA}.
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@Configuration
@ComponentScan(basePackageClasses =
                   { ball.spring.mysqld.MysqldComponent.class })
@ConditionalOnProperty(name = "mysqld.home", havingValue = "")
public class EntityManagerFactoryConfiguration
             extends EntityManagerFactoryDependsOnPostProcessor {
    @Autowired private Process mysqld;

    /**
     * Sole constructor.
     */
    public EntityManagerFactoryConfiguration() { super("mysqld"); }

    @Override
    public String toString() { return super.toString(); }
}
