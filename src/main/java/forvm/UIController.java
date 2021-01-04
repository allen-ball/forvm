package forvm;
/*-
 * ##########################################################################
 * forvm Blog Publishing Platform
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2018 - 2021 Allen D. Ball
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ##########################################################################
 */
import ball.spring.AbstractController;
import forvm.entity.Article;
import forvm.repository.ArticleRepository;
import forvm.repository.AuthorRepository;
import forvm.repository.CredentialRepository;
import java.security.Principal;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * UI {@link Controller} implementation.
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Controller
@NoArgsConstructor @ToString @Log4j2
public class UIController extends AbstractController {
    private static final String ERRORS = "errors";
    private static final String FORM = "form";
    private static final String PAGE = "page";

    @Autowired private AuthorRepository authorRepository = null;
    @Autowired private ArticleRepository articleRepository = null;
    @Autowired private CredentialRepository credentialRepository = null;
    @Autowired private MarkdownService service = null;
    @Autowired private PasswordEncoder encoder = null;

    private int page_size = 8;

    @RequestMapping(value = { "/" })
    @PreAuthorize("permitAll()")
    public String root() { return "redirect:/index.html"; }

    @RequestMapping(value = { "/index.html", "/index.htm", "/index" })
    @PreAuthorize("permitAll()")
    public String index() { return "redirect:/articles/"; }

    @RequestMapping(value = {
                        "/articles", "/article/{slug}",
                        "/authors",
                        "/preview", "/preview/{slug}"
                    })
    @PreAuthorize("permitAll()")
    public String redirect(HttpServletRequest request) {
        return "redirect:" + request.getServletPath() + "/";
    }

    @RequestMapping(method = { GET }, value = { "/articles/" })
    @PreAuthorize("permitAll()")
    public String articles(Model model,
                           HttpServletRequest request,
                           @RequestParam Optional<String> author,
                           @RequestParam Optional<Integer> page,
                           RedirectAttributes redirect) {
        var view = getViewName();

        if (page.isPresent()) {
            var pr = PageRequest.of(page.get() - 1, page_size, Sort.Direction.DESC, "slug");
            Page<?> articles = null;

            if (author.isPresent()) {
                var slug = author.get();

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

    @RequestMapping(method = { GET }, value = { "/article/{slug}/" })
    @PreAuthorize("permitAll()")
    public String article(Model model, @PathVariable String slug) {
        model
            .addAttribute("article", articleRepository.findBySlug(slug).get());

        return getViewName();
    }

    @RequestMapping(method = { GET }, value = { "/authors/" })
    @PreAuthorize("permitAll()")
    public String authors(Model model,
                          HttpServletRequest request,
                          @RequestParam Optional<Integer> page,
                          RedirectAttributes redirect) {
        var view = getViewName();

        if (page.isPresent()) {
            var  pr = PageRequest.of(page.get() - 1, page_size);
            var authors = authorRepository.findAll(pr);

            model.addAttribute("authors", authors);
            model.addAttribute(PAGE, authors);
        } else {
            redirect.addAttribute(PAGE, String.valueOf(1));
            redirect.mergeAttributes(request.getParameterMap());

            view = "redirect:" + request.getServletPath();
        }

        return view;
    }

    @RequestMapping(method = { GET }, value = { "/preview/" })
    @PreAuthorize("hasAuthority('AUTHOR')")
    public String preview(Model model) {
        model.addAttribute(FORM, new PreviewForm());

        return getViewName();
    }

    @RequestMapping(method = { POST }, value = { "/preview/" })
    @PreAuthorize("hasAuthority('AUTHOR')")
    public String previewPOST(Model model,
                              Principal principal, HttpSession session,
                              HttpServletRequest request,
                              @Valid PreviewForm form, BindingResult result) {
        var view = getViewName();

        try {
            if (! result.hasErrors()) {
                var email = principal.getName();
                var name = form.getFile().getOriginalFilename();
                var  slug = FilenameUtils.getBaseName(name);
                var article = new Article();

                article.setAuthor(authorRepository.findById(email).get());
                article.setEmail(article.getAuthor().getEmail());

                service.compile(name, form.getFile().getBytes(),
                                request.getServletPath(), slug, article);

                session.setAttribute(article.getSlug(), article);

                view =
                    "redirect:" + request.getServletPath()
                    + article.getSlug() + "/";
            } else {
                model.addAttribute(FORM, form);
            }
        } catch (Exception exception) {
            model.addAttribute(ERRORS, exception.getMessage());
        }

        return view;
    }

    @RequestMapping(method = { GET }, value = { "/preview/{slug}/" })
    @PreAuthorize("hasAuthority('AUTHOR')")
    public String article(Model model,
                          HttpSession session,
                          @PathVariable String slug) {
        var article = Optional.ofNullable((Article) session.getAttribute(slug));

        model.addAttribute("article", article.get());

        return getViewName();
    }

    @RequestMapping(method = { GET }, value = { "/login" })
    @PreAuthorize("permitAll()")
    public String login(Model model, HttpSession session) {
        model.addAttribute(FORM, new LoginForm());

        return getViewName();
    }

    @RequestMapping(method = { GET }, value = { "/password" })
    @PreAuthorize("isAuthenticated()")
    public String password(Model model) {
        model.addAttribute(FORM, new ChangePasswordForm());

        return getViewName();
    }

    @RequestMapping(method = { POST }, value = { "/password" })
    @PreAuthorize("isAuthenticated()")
    public String passwordPOST(Model model, Principal principal,
                               @Valid ChangePasswordForm form, BindingResult result) {
        var credential =
            credentialRepository.findById(principal.getName())
            .orElseThrow(() -> new AuthorizationServiceException("Unauthorized"));

        try {
            if (result.hasErrors()) {
                throw new RuntimeException(String.valueOf(result.getAllErrors()));
            }

            if (! (Objects.equals(form.getUsername(), principal.getName())
                   && encoder.matches(form.getPassword(), credential.getPassword()))) {
                throw new AccessDeniedException("Invalid user name and password");
            }

            if (! (form.getNewPassword() != null
                   && Objects.equals(form.getNewPassword(), form.getRepeatPassword()))) {
                throw new RuntimeException("Repeated password does not match new password");
            }

            if (encoder.matches(form.getNewPassword(), credential.getPassword())) {
                throw new RuntimeException("New password must be different than old");
            }

            credential.setPassword(encoder.encode(form.getNewPassword()));
            credentialRepository.save(credential);
        } catch (Exception exception) {
            model.addAttribute(FORM, form);
            model.addAttribute(ERRORS, exception.getMessage());
        }

        return getViewName();
    }
}
