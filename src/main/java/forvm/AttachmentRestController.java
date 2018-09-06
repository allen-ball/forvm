/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm;

import forvm.entity.Attachment;
import forvm.repository.ArticleRepository;
import forvm.repository.AttachmentRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
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
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@RestController
@ComponentScan(basePackageClasses =
                   { ball.spring.mysqld.MysqldComponent.class })
@RequestMapping(value = { "/article/{slug}/**" },
                produces = "application/octet-stream")
public class AttachmentRestController {
    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired private ArticleRepository articleRepository;
    @Autowired private AttachmentRepository attachmentRepository;

    /**
     * Sole constructor.
     */
    public AttachmentRestController() { super(); }

    @RequestMapping(method = { GET })
    @PreAuthorize("permitAll()")
    public byte[] get(HttpServletRequest request,
                      @PathVariable String slug) {
        String uri = request.getRequestURI();
        String path = uri.substring(uri.indexOf(slug) + slug.length());
        Optional<Attachment> attachment =
            articleRepository.findBySlug(slug)
            .flatMap(t -> attachmentRepository.findByArticleAndPath(t.getId(), path));

        return attachment.get().getContent();
    }

    @ExceptionHandler({ NoSuchElementException.class })
    @ResponseStatus(value = NOT_FOUND, reason = "Resource not found")
    public void handleNOT_FOUND() { }

    @Override
    public String toString() { return super.toString(); }
}
