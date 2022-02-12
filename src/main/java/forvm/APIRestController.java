package forvm;
/*-
 * ##########################################################################
 * forvm Blog Publishing Platform
 * %%
 * Copyright (C) 2018 - 2022 Allen D. Ball
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
import forvm.entity.Article;
import forvm.entity.Author;
import forvm.repository.ArticleRepository;
import forvm.repository.AuthorRepository;
import java.security.Principal;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * API {@link RestController} implementation.
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 */
@RestController
@RequestMapping(value = { "/api/v1" }, produces = APPLICATION_JSON_VALUE)
@NoArgsConstructor @ToString @Log4j2
public class APIRestController {
    @Autowired private AuthorRepository authorRepository = null;
    @Autowired private ArticleRepository articleRepository = null;
    @Autowired private MarkdownService service = null;

    @RequestMapping(method = { PUT }, value = { "/author", "/author/{slug}" })
    @PreAuthorize("hasAuthority('AUTHOR')")
    @Transactional
    public ResponseEntity<String> author(Principal principal, HttpMethod method, @PathVariable Optional<String> slug, @RequestParam MultipartFile file) throws Exception {
        var email = principal.getName();
        Author author = null;

        switch (method) {
        case PUT:
            author = authorRepository.findById(email).get();
            break;

        default:
            throw new MethodNotAllowedException(String.valueOf(method));
        }

        var name = file.getOriginalFilename();

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
    public ResponseEntity<String> article(Principal principal, HttpMethod method, @PathVariable String slug) throws Exception {
        var email = principal.getName();

        switch (method) {
        case DELETE:
            var article = articleRepository.findBySlug(slug).get();

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
    public ResponseEntity<String> article(Principal principal, HttpMethod method, @PathVariable Optional<String> slug, @RequestParam MultipartFile file) throws Exception {
        var email = principal.getName();
        var name = file.getOriginalFilename();

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

        service.compile(name, file.getBytes(), "/article", slug.get(), article);

        if (email.equals(article.getAuthor().getEmail())) {
            articleRepository.save(article);
        } else {
            throw new ForbiddenException(slug.get());
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ExceptionHandler({ NoSuchElementException.class })
    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found")
    public void handleNOT_FOUND() { }

    @ExceptionHandler({ AccessDeniedException.class, SecurityException.class })
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
