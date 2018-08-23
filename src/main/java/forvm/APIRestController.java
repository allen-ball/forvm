/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API {@link RestController} implementation
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@RestController
@RequestMapping(value = { "/api/v1" })
public class APIRestController {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Sole constructor.
     */
    public APIRestController() { super(); }

    @Override
    public String toString() { return super.toString(); }
}
