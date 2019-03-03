/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package forvm;

import forvm.entity.Article;
import forvm.entity.Author;
import forvm.repository.ArticleRepository;
import forvm.repository.AuthorRepository;
import java.security.Principal;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * API {@link RestController} implementation
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@RestController
@ComponentScan(basePackageClasses =
                   { ball.spring.mysqld.MysqldComponent.class })
@RequestMapping(value = { "/api/v1" }, produces = "application/json")
@NoArgsConstructor @ToString
public class APIRestController {
    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired private AuthorRepository authorRepository;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private MarkdownService service;

    @RequestMapping(method = { PUT },
                    value = { "/author", "/author/{slug}" })
    @PreAuthorize("hasAuthority('AUTHOR')")
    @Transactional
    public ResponseEntity<String> author(Principal principal,
                                         HttpMethod method,
                                         @PathVariable Optional<String> slug,
                                         @RequestParam MultipartFile file) throws Exception {
        String email = principal.getName();
        Author author = null;

        switch (method) {
        case PUT:
            author = authorRepository.findById(email).get();
            break;

        default:
            throw new MethodNotAllowedException(String.valueOf(method));
        }

        String name = file.getOriginalFilename();

        if (! slug.isPresent()) {
            slug = Optional.of(FilenameUtils.getBaseName(name));
        }

        service.compile(name, file.getBytes(), slug.get(), author);

        if (email.equals(author.getEmail())) {
            authorRepository.save(author);
        } else {
            throw new ForbiddenException(slug.get());
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(method = { DELETE }, value = { "/article/{slug}" })
    @PreAuthorize("hasAuthority('AUTHOR')")
    @Transactional
    public ResponseEntity<String> article(Principal principal,
                                          HttpMethod method,
                                          @PathVariable String slug) throws Exception {
        String email = principal.getName();

        switch (method) {
        case DELETE:
            Article article = articleRepository.findBySlug(slug).get();

            if (email.equals(article.getAuthor().getEmail())) {
                articleRepository.delete(article);
            } else {
                throw new ForbiddenException(slug);
            }
            break;

        default:
            throw new MethodNotAllowedException(String.valueOf(method));
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(method = { POST, PUT },
                    value = { "/article", "/article/{slug}" })
    @PreAuthorize("hasAuthority('AUTHOR')")
    @Transactional
    public ResponseEntity<String> article(Principal principal,
                                          HttpMethod method,
                                          @PathVariable Optional<String> slug,
                                          @RequestParam MultipartFile file) throws Exception {
        String email = principal.getName();
        String name = file.getOriginalFilename();

        if (! slug.isPresent()) {
            slug = Optional.of(FilenameUtils.getBaseName(name));
        }

        Article article = null;

        switch (method) {
        case POST:
            if (articleRepository.findBySlug(slug.get()).isPresent()) {
                throw new ConflictException(slug.get());
            }

            article = new Article();
            article.setAuthor(authorRepository.findById(email).get());
            article.setEmail(article.getAuthor().getEmail());
            break;

        case PUT:
            article = articleRepository.findBySlug(slug.get()).get();
            break;

        default:
            throw new MethodNotAllowedException(String.valueOf(method));
        }

        service.compile(name, file.getBytes(),
                        "/article", slug.get(), article);

        if (email.equals(article.getAuthor().getEmail())) {
            articleRepository.save(article);
        } else {
            throw new ForbiddenException(slug.get());
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ExceptionHandler({ NoSuchElementException.class })
    @ResponseStatus(value = HttpStatus.NOT_FOUND,
                    reason = "Resource not found")
    public void handleNOT_FOUND() { }

    @ExceptionHandler({ SecurityException.class })
    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Forbidden")
    public void handleFORBIDDEN() { }

    @ResponseStatus(value = HttpStatus.CONFLICT)
    public static class ConflictException extends RuntimeException {
        private static final long serialVersionUID = -3019016947330828859L;

        private ConflictException(String message) { super(message, null); }
    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public static class ForbiddenException extends RuntimeException {
        private static final long serialVersionUID = -3698565165488957902L;

        private ForbiddenException(String message) { super(message, null); }
    }

    @ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
    public static class MethodNotAllowedException extends RuntimeException {
        private static final long serialVersionUID = 8201840514907306612L;

        private MethodNotAllowedException(String message) {
            super(message, null);
        }
    }
}
