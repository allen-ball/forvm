/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
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

    @RequestMapping(value = { "/" })
    public String root() { return "redirect:/index.html"; }

    @RequestMapping(value = { "/index.html", "/index.htm", "/index" })
    public String index() { return "redirect:/posts/page/1"; }

    @RequestMapping(value = { "/posts", "/posts/by-{type}/{slug}" })
    public String posts(HttpServletRequest request) {
        return "redirect:" + request.getServletPath() + "/page/1";
    }

    @RequestMapping(value = {
                        "/posts/page/{page}",
                        "/posts/by-{type}/{slug}/page/{page}"
                    })
    public String posts(@PathVariable(required = false) String type,
                        @PathVariable(required = false) String slug,
                        @PathVariable int page,
                        Model model) {
        return "boilerplate";
    }

    @RequestMapping(value = { "/post/{slug}" })
    public String post(@PathVariable("slug") String slug, Model model) {
        return "boilerplate";
    }

    @RequestMapping(value = { "/authors" })
    public String authors(Model model) {
        return "boilerplate";
    }

    @RequestMapping(value = { "/tags" })
    public String tags(Model model) {
        return "boilerplate";
    }

    @Override
    public String toString() { return super.toString(); }
}
