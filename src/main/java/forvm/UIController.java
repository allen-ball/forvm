/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm;

import ball.spring.BootstrapUI;
import com.vladsch.flexmark.ast.Document;
import forvm.entity.Credential;
import forvm.repository.ArticleRepository;
import forvm.repository.AuthorRepository;
import forvm.repository.CredentialRepository;
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
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    private static final String EXCEPTION = "exception";
    private static final String FORM = "form";
    private static final String PAGE = "page";

    private static final String[] CSS =
        new String[] { "/css/prism.css", "/css/style.css" };
    private static final String[] JS = new String[] { "/js/prism.js" };

    @Value("${application.name}") private String brand;
    @Autowired private AuthorRepository authorRepository;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private CredentialRepository credentialRepository;
    @Autowired private MarkdownService service;
    @Autowired private PasswordEncoder encoder;

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
            PageRequest pr =
                PageRequest.of(page.get() - 1, page_size,
                               Sort.Direction.DESC, "slug");
            Page<?> articles = null;

            if (author.isPresent()) {
                String slug = author.get();

                articles =
                    articleRepository
                    .findByAuthor(pr, authorRepository.findBySlug(slug).get());
            } else {
                articles = articleRepository.findAll(pr);
            }

            model.addAttribute("articles", articles);
            model.addAttribute(PAGE, articles);
        } else {
            redirect.addAttribute(PAGE, String.valueOf(1));
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
            model.addAttribute(PAGE, authors);
        } else {
            redirect.addAttribute(PAGE, String.valueOf(1));
            redirect.mergeAttributes(request.getParameterMap());

            view = "redirect:" + request.getServletPath();
        }

        return view;
    }

    @RequestMapping(method = { GET }, value = { "/preview" })
    @PreAuthorize("hasAuthority('AUTHOR')")
    public String preview(Model model) {
        model.addAttribute(FORM, new PreviewForm());

        return VIEW;
    }

    @RequestMapping(method = { POST }, value = { "/preview" })
    @PreAuthorize("hasAuthority('AUTHOR')")
    public String previewPOST(Model model,
                              PreviewForm form, BindingResult result) {
        try {
            if (! result.hasErrors()) {
                String markdown = new String(form.getFile().getBytes(), UTF_8);

                model.addAttribute("markdown", markdown);

                Document document = service.parse(markdown);
                Map<String,List<String>> yaml = service.getYamlFrom(document);

                model.addAttribute("yaml", yaml);

                CharSequence html = service.htmlRender(document, null);

                model.addAttribute("html", html);
            } else {
                model.addAttribute(FORM, form);
            }
        } catch (Exception exception) {
            model.addAttribute(EXCEPTION, exception.getMessage());
        }

        return VIEW;
    }

    @RequestMapping(method = { GET }, value = { "/login" })
    @PreAuthorize("permitAll()")
    public String login(Model model) {
        model.addAttribute(FORM, new LoginForm());

        return VIEW;
    }

    @RequestMapping(method = { GET }, value = { "/password" })
    @PreAuthorize("isAuthenticated()")
    public String password(Model model) {
        model.addAttribute(FORM, new ChangePasswordForm());

        return VIEW;
    }

    @RequestMapping(method = { POST }, value = { "/password" })
    @PreAuthorize("isAuthenticated()")
    public String passwordPOST(Model model,
                               ChangePasswordForm form, BindingResult result) {
        try {
            if (result.hasErrors()) {
                throw new RuntimeException(String.valueOf(result.getAllErrors()));
            }

            if (! (form.getNewPassword() != null
                   && form.getNewPassword().equals(form.getRepeatPassword()))) {
                throw new RuntimeException("Repeated password does not match new password");
            }

            Credential credential =
                credentialRepository.findById(form.getUsername()).get();

            if (! encoder.matches(form.getPassword(), credential.getPassword())) {
                throw new RuntimeException("Invalid password");
            }

            credential.setPassword(encoder.encode(form.getNewPassword()));

            credentialRepository.save(credential);
        } catch (Exception exception) {
            model.addAttribute(FORM, form);
            model.addAttribute(EXCEPTION, exception);
        }

        return VIEW;
    }

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
