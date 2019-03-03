/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
 * Attachment {@link RestController} implementation
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@RestController
@ComponentScan(basePackageClasses =
                   { ball.spring.mysqld.MysqldComponent.class })
@RequestMapping(produces = "application/octet-stream")
@NoArgsConstructor @ToString
public class AttachmentRestController {
    private static final Logger LOGGER = LogManager.getLogger();

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
