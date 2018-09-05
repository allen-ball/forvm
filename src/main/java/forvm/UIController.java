/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm;

import forvm.repository.ArticleRepository;
import forvm.repository.AuthorRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
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
import org.springframework.web.multipart.MultipartFile;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * UI {@link Controller} implementation
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@Controller
@ComponentScan(basePackageClasses =
                   { ball.spring.mysqld.MysqldComponent.class })
public class UIController {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String VIEW = UIController.class.getSimpleName();

    @Autowired private AuthorRepository authorRepository;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private Parser parser;
    @Autowired private HtmlRenderer renderer;

    private int articles_per_page = 6;

    /**
     * Sole constructor.
     */
    public UIController() { super(); }

    @RequestMapping(value = { "/" })
    public String root() { return "redirect:/index.html"; }

    @RequestMapping(value = { "/index.html", "/index.htm", "/index" })
    public String index() { return "redirect:/articles"; }

    @RequestMapping(value = { "/articles" })
    public String articles(HttpServletRequest request,
                           @RequestParam Optional<Integer> page,
                           Model model) {
        String view = VIEW;

        if (page.isPresent()) {
            Page<?> articles =
                articleRepository.findAll(PageRequest.of(page.get() - 1,
                                                         articles_per_page));

            model.addAttribute("articles", articles);
            model.addAttribute("compass", new Compass(request, articles));
        } else {
            view = "redirect:" + request.getServletPath() + "?page=" + 1;
        }

        return view;
    }

    @RequestMapping(value = { "/article/{slug}" })
    public String article(@PathVariable String slug, Model model) {
        model.addAttribute("article",
                           articleRepository.findBySlug(slug).get());
        model.addAttribute("compass", new Compass());

        return VIEW;
    }

    @RequestMapping(value = { "/authors" })
    public String authors(Model model) {
        model.addAttribute("authors", authorRepository.findAll());
        model.addAttribute("compass", new Compass());

        return VIEW;
    }

    @RequestMapping(value = { "/author/{slug}" })
    public String author(@PathVariable String slug, Model model) {
        model.addAttribute("author", authorRepository.findBySlug(slug).get());
        model.addAttribute("compass", new Compass());

        return VIEW;
    }

    @RequestMapping(method = { GET }, value = { "/preview" })
    public String preview(Model model) {
        model.addAttribute("compass", new Compass());

        return VIEW;
    }

    @RequestMapping(method = { POST }, value = { "/preview" })
    public String previewPOST(@RequestParam("file") MultipartFile file,
                              Model model) {
        try {
            String markdown = new String(file.getBytes(), UTF_8);

            model.addAttribute("markdown", markdown);

            Node document = parser.parse(markdown);
            YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();

            document.accept(visitor);

            StringBuilder html = new StringBuilder();

            renderer.render(document, html);

            model.addAttribute("html", html);
        } catch (Exception exception) {
            model.addAttribute("error", exception.getMessage());
        }

        model.addAttribute("compass", new Compass());

        return VIEW;
    }

    @RequestMapping(method = { GET }, value = { "/login" })
    public String login(Model model) {
        model.addAttribute("compass", new Compass());

        return VIEW;
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
