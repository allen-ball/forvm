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
    public String root() { return "redirect:/posts"; }

    @RequestMapping(value = { "/index.html", "/index", "/index.htm" })
    public String index() { return "redirect:/posts"; }

    @RequestMapping(value = { "/posts" })
    public String posts(Model model) {
        return "boilerplate";
    }

    @RequestMapping(value = { "/post/{slug}" })
    public String post(@PathVariable String slug, Model model) {
        return "boilerplate";
    }

    @RequestMapping(value = { "/authors" })
    public String authors(Model model) {
        return "boilerplate";
    }

    @RequestMapping(value = { "/author/{slug}" })
    public String author(@PathVariable String slug, Model model) {
        return "boilerplate";
    }

    @RequestMapping(value = { "/tags" })
    public String tags(Model model) {
        return "boilerplate";
    }

    @RequestMapping(value = { "/tag/{slug}" })
    public String tag(@PathVariable String slug, Model model) {
        return "boilerplate";
    }

    @Override
    public String toString() { return super.toString(); }
}
