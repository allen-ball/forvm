/*
 * $Id$
 *
 * Copyright 2018 - 2020 Allen D. Ball.  All rights reserved.
 */
package forvm;

import forvm.entity.Article;
import forvm.entity.Attachment;
import forvm.repository.ArticleRepository;
import forvm.repository.AttachmentRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Attachment {@link RestController} implementation.
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@RestController
@ComponentScan(basePackageClasses =
                   { ball.spring.mysqld.MysqldComponent.class })
@NoArgsConstructor @ToString @Log4j2
public class AttachmentRestController {
    @Autowired private ArticleRepository articleRepository;
    @Autowired private AttachmentRepository attachmentRepository;

    @RequestMapping(method = { GET }, value = { "/article/{slug}/**" })
    @PreAuthorize("permitAll()")
    public byte[] get(HttpServletRequest request,
                      @PathVariable String slug) throws Exception {
        String uri = request.getRequestURI();
        String path = uri.substring(uri.indexOf(slug) + slug.length());
        Optional<Attachment> attachment =
            articleRepository.findBySlug(slug)
            .flatMap(t -> attachmentRepository.findByArticleAndPath(t, path));

        return attachment.get().getContent();
    }

    @RequestMapping(method = { GET }, value = { "/preview/{slug}/**" })
    @PreAuthorize("hasAuthority('AUTHOR')")
    public byte[] get(HttpSession session,
                      HttpServletRequest request,
                      @PathVariable String slug) throws Exception {
        String uri = request.getRequestURI();
        String path = uri.substring(uri.indexOf(slug) + slug.length());
        Optional<Attachment> attachment =
            Optional.ofNullable((Article) session.getAttribute(slug)).get()
            .getAttachments().stream()
            .filter(t -> t.getPath().equalsIgnoreCase(path))
            .findFirst();

        return attachment.get().getContent();
    }

    @ExceptionHandler({ NoSuchElementException.class })
    @ResponseStatus(value = NOT_FOUND, reason = "Resource not found")
    public void handleNOT_FOUND() { }
}
