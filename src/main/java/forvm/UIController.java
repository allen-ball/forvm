/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm;

import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * UI {@link Controller} implementation
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@Controller
public class UIController {
    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired private APIRestController api;

    /**
     * Sole constructor.
     */
    public UIController() { super(); }

    @RequestMapping(value = { "/", "/index", "/index.htm" })
    public String root() { return "redirect:/index.html"; }

    @RequestMapping(value = { "/index.html" })
    public String index(Model model) {
        return null;
    }

    @Override
    public String toString() { return super.toString(); }
}
