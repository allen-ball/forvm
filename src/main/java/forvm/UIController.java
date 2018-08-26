/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
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

    public static final String BOILERPLATE ="boilerplate";

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
        return BOILERPLATE;
    }

    @RequestMapping(value = { "/post/{slug}" })
    public String post(@PathVariable("slug") String slug, Model model) {
        return BOILERPLATE;
    }

    @RequestMapping(value = { "/authors" })
    public String authors(Model model) {
        return BOILERPLATE;
    }

    @RequestMapping(value = { "/tags" })
    public String tags(Model model) {
        return BOILERPLATE;
    }

    @RequestMapping(value = { "/preview" })
    public String preview(Model model) {
        return BOILERPLATE;
    }

    @RequestMapping(value = { "/login" })
    public String login() { return BOILERPLATE; }

    @RequestMapping(value = { "/logout" })
    public String logout (HttpServletRequest request,
                          HttpServletResponse response) {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        new SecurityContextLogoutHandler()
            .logout(request, response, authentication);

        return "redirect:/";
    }

    @Override
    public String toString() { return super.toString(); }
}
