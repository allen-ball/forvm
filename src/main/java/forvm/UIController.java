/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm;

import ball.spring.BootstrapUI;
import com.vladsch.flexmark.ast.Document;
import forvm.repository.ArticleRepository;
import forvm.repository.AuthorRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static java.nio.charset.StandardCharsets.UTF_8;
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
public class UIController extends BootstrapUI {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String[] CSS =
        new String[] { "/css/prism.css", "/css/style.css" };
    private static final String[] JS = new String[] { "/js/prism.js" };

    @Value("${application.name}") private String brand;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private MarkdownService service;

    private int page_size = 6;

    /**
     * Sole constructor.
     */
    public UIController() { super(); }

    @Override
    public String[] css() { return CSS; }

    @Override
    public String[] js() { return JS; }

    @Override
    public String brand() { return brand; }

    @Override
    public String template() {
        return getClass().getPackage().getName().replaceAll("[.]", "-");
    }

    @RequestMapping(value = { "/" })
    @PreAuthorize("permitAll()")
    public String root() { return "redirect:/index.html"; }

    @RequestMapping(value = { "/index.html", "/index.htm", "/index" })
    @PreAuthorize("permitAll()")
    public String index() { return "redirect:/articles"; }

    @RequestMapping(method = { GET }, value = { "/articles" })
    @PreAuthorize("permitAll()")
    public String articles(Model model, RedirectAttributes redirect,
                           HttpServletRequest request,
                           @RequestParam Optional<String> author,
                           @RequestParam Optional<Integer> page) {
        String view = VIEW;

        if (page.isPresent()) {
            PageRequest pr = PageRequest.of(page.get() - 1, page_size);
            Page<?> articles = articleRepository.findAll(pr);

            model.addAttribute("articles", articles);
            model.addAttribute("page", articles);
        } else {
            redirect.addAttribute("page", String.valueOf(1));
            redirect.mergeAttributes(request.getParameterMap());

            view = "redirect:" + request.getServletPath();
        }

        return view;
    }

    @RequestMapping(method = { GET }, value = { "/article/{slug}" })
    @PreAuthorize("permitAll()")
    public String article(Model model, @PathVariable String slug) {
        model
            .addAttribute("article", articleRepository.findBySlug(slug).get());

        return VIEW;
    }

    @RequestMapping(method = { GET }, value = { "/authors" })
    @PreAuthorize("permitAll()")
    public String authors(Model model, RedirectAttributes redirect,
                          HttpServletRequest request,
                          @RequestParam Optional<Integer> page) {
        String view = VIEW;

        if (page.isPresent()) {
            PageRequest pr = PageRequest.of(page.get() - 1, page_size);
            Page<?> authors = authorRepository.findAll(pr);

            model.addAttribute("authors", authors);
            model.addAttribute("page", authors);
        } else {
            redirect.addAttribute("page", String.valueOf(1));
            redirect.mergeAttributes(request.getParameterMap());

            view = "redirect:" + request.getServletPath();
        }

        return view;
    }

    @RequestMapping(method = { GET }, value = { "/preview" })
    @PreAuthorize("hasAuthority('AUTHOR')")
    public String preview() { return VIEW; }

    @RequestMapping(method = { POST }, value = { "/preview" })
    @PreAuthorize("hasAuthority('AUTHOR')")
    public String previewPOST(Model model, @RequestParam MultipartFile file) {
        try {
            String markdown = new String(file.getBytes(), UTF_8);

            model.addAttribute("markdown", markdown);

            Document document = service.parse(markdown);
            Map<String,List<String>> yaml = service.getYamlFrom(document);
            CharSequence html = service.htmlRender(document, null);

            model.addAttribute("html", html);
        } catch (Exception exception) {
            model.addAttribute("exception", exception.getMessage());
        }

        return VIEW;
    }

    @RequestMapping(method = { GET }, value = { "/login" })
    @PreAuthorize("permitAll()")
    public String login() { return VIEW; }

    @RequestMapping(value = { "/logout" })
    @PreAuthorize("permitAll()")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response) {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        new SecurityContextLogoutHandler()
            .logout(request, response, authentication);

        return "redirect:/";
    }
}
