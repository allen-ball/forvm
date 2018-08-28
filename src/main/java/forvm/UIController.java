/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm;

import forvm.repository.AuthorRepository;
import forvm.repository.PostRepository;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * UI {@link Controller} implementation
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@Controller
public class UIController implements ErrorController {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String BOILERPLATE ="boilerplate";

    private static final int PAGE_SIZE = 6;

    @Autowired private AuthorRepository authorRepository;
    @Autowired private PostRepository postRepository;

    /**
     * Sole constructor.
     */
    public UIController() { super(); }

    @RequestMapping(value = { "/" })
    public String root() { return "redirect:/index.html"; }

    @RequestMapping(value = { "/index.html", "/index.htm", "/index" })
    public String index() { return "redirect:/posts/"; }

    @RequestMapping(value = { "/posts", "/authors" })
    public String containers(HttpServletRequest request) {
        return "redirect:" + request.getServletPath() + "/";
    }

    @RequestMapping(value = { "/posts/" })
    public String posts(HttpServletRequest request,
                        @RequestParam(required = false) Integer page,
                        Model model) {
        if (page == null) {
            return "redirect:" + request.getServletPath() + "?page=1";
        }

        Page<?> posts =
            postRepository.findAll(PageRequest.of(page - 1, PAGE_SIZE));

        model.addAttribute("posts", posts);
        model.addAttribute("compass", new Compass(request, posts));

        return BOILERPLATE;
    }

    @RequestMapping(value = { "/post/{slug}" })
    public String post(@PathVariable("slug") String slug, Model model) {
        model.addAttribute("post", postRepository.findBySlug(slug).get());
        model.addAttribute("compass", new Compass());

        return BOILERPLATE;
    }

    @RequestMapping(value = { "/authors/" })
    public String authors(Model model) {
        model.addAttribute("authors", authorRepository.findAll());
        model.addAttribute("compass", new Compass());

        return BOILERPLATE;
    }

    @RequestMapping(value = { "/author/{slug}" })
    public String author(@PathVariable("slug") String slug, Model model) {
        model.addAttribute("author", authorRepository.findBySlug(slug).get());
        model.addAttribute("compass", new Compass());

        return BOILERPLATE;
    }

    @RequestMapping(value = { "/preview" })
    public String preview(Model model) {
        model.addAttribute("compass", new Compass());

        return BOILERPLATE;
    }

    @RequestMapping(value = { "/login" })
    public String login(Model model) {
        model.addAttribute("compass", new Compass());

        return BOILERPLATE;
    }

    @RequestMapping(value = { "/logout" })
    public String logout (HttpServletRequest request,
                          HttpServletResponse response) {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        new SecurityContextLogoutHandler()
            .logout(request, response, authentication);

        return "redirect:/";
    }

    @RequestMapping(value = { "/error" })
    public String error(HttpServletRequest request, Model model) {
        model.addAttribute("compass", new Compass());

        return BOILERPLATE;
    }

    @Override
    public String getErrorPath() { return "/error"; }

    @ExceptionHandler({ NoSuchElementException.class })
    @ResponseStatus(value = NOT_FOUND, reason = "Resource not found")
    public void handleNOT_FOUND() { }

    @Override
    public String toString() { return super.toString(); }

    /**
     * {@bean.info}
     */
    protected class Compass {
        private static final String HREF = "%s?page=%d";

        private final String path;
        private final Page<?> page;

        private Compass(HttpServletRequest request, Page<?> page) {
            this.path = (request != null) ? request.getServletPath() : null;
            this.page = page;
        }

        private Compass() { this(null, null); }

        public String getFirstPage() {
            return href(1);
        }

        public String getPrevPage() {
            String href = null;

            if (page != null && page.hasPrevious()) {
                href = href(page.getNumber());
            }

            return href;
        }

        public String getNextPage() {
            String href = null;

            if (page != null && page.hasNext()) {
                href = href(page.getNumber() + 2);
            }

            return href;
        }

        public String getLastPage() {
            String href = null;

            if (page != null) {
                href = href(Math.max(page.getTotalPages(), 1));
            } else {
                href = getFirstPage();
            }

            return href;
        }

        private String href(int page) {
            String string = null;

            if (path != null) {
                string = String.format(HREF, path, page);
            }

            return string;
        }

        @Override
        public String toString() { return super.toString(); }
    }
}
