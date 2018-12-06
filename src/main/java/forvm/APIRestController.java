/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm;

import ball.io.FileImpl;
import com.vladsch.flexmark.ast.Document;
import forvm.entity.Article;
import forvm.entity.Attachment;
import forvm.entity.Author;
import forvm.repository.ArticleRepository;
import forvm.repository.AuthorRepository;
import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * API {@link RestController} implementation
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@RestController
@ComponentScan(basePackageClasses =
                   { ball.spring.mysqld.MysqldComponent.class })
@RequestMapping(value = { "/api/v1" }, produces = "application/json")
public class APIRestController {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String README_MD = "README.md";
    private static final String SLASH = "/";
    private static final URI ROOT = URI.create(SLASH);

    @Autowired private AuthorRepository authorRepository;
    @Autowired private ArticleRepository articleRepository;
    @Autowired private Tika tika;
    @Autowired private MarkdownService service;

    /**
     * Sole constructor.
     */
    public APIRestController() { super(); }

    @RequestMapping(method = { PUT }, value = { "/author" })
    @PreAuthorize("hasAuthority('AUTHOR')")
    @Transactional
    public ResponseEntity<String> author(Principal principal,
                                         HttpMethod method,
                                         @RequestParam MultipartFile file) throws Exception {
        Author author = null;

        switch (method) {
        case PUT:
            author = authorRepository.findById(principal.getName()).get();
            break;

        default:
            throw new MethodNotAllowedException(String.valueOf(method));
        }

        String name = file.getOriginalFilename();
        String markdown = new String(file.getBytes(), UTF_8);
        Document document = service.parse(markdown);
        Map<String,List<String>> yaml = service.getYamlFrom(document);

        if (yaml.containsKey("email")) {
            String email =
                yaml.get("email").stream().collect(Collectors.joining());

            if (! author.getEmail().equals(email)) {
                throw new ForbiddenException(email);
            }
        }

        String slug = FileImpl.getNameBase(name);

        if (yaml.containsKey("slug")) {
            slug = yaml.get("slug").stream().collect(Collectors.joining());
        }

        author.setSlug(slug);

        if (yaml.containsKey("name")) {
            author.setName(yaml.get("name")
                           .stream().collect(Collectors.joining()));
        }

        author.setMarkdown(markdown);
        author.setHtml(service.htmlRender(document, null).toString());

        authorRepository.save(author);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(method = { POST, PUT }, value = { "/article" })
    @PreAuthorize("hasAuthority('AUTHOR')")
    @Transactional
    public ResponseEntity<String> article(Principal principal,
                                          HttpMethod method,
                                          @RequestParam MultipartFile file) throws Exception {
        String name = file.getOriginalFilename();
        String slug = FileImpl.getNameBase(name);
        byte[] bytes = file.getBytes();
        String type = tika.detect(bytes, name);
        ZipFile zip = null;

        try {
            ZipArchiveEntry source = null;
            String markdown = null;

            if (type != null) {
                switch (type) {
                case "application/zip":
                    zip =
                        new ZipFile(new SeekableInMemoryByteChannel(bytes),
                                    name, UTF_8.name(), true);
                    source = zip.getEntry(README_MD);
                    markdown =
                        new Scanner(zip.getInputStream(source), UTF_8.name())
                        .useDelimiter("\\A").next();
                    break;

                default:
                    markdown = new String(bytes, UTF_8);
                    break;
                }
            } else {
                throw new UnsupportedMediaTypeException("unknown");
            }

            TreeMap<String,String> map = new TreeMap<>();

            if (zip != null) {
                for (ZipArchiveEntry entry :
                         Collections.list(zip.getEntries())) {
                    if (! Arrays.asList(README_MD).contains(entry.getName())) {
                        if (! map.containsKey(entry.getName())) {
                            URI uri =
                                ROOT.resolve(entry.getName()).normalize();

                            map.put(entry.getName(), uri.getPath());
                        }
                    }
                }
            }

            Document document = service.parse(markdown);
            Map<String,List<String>> yaml = service.getYamlFrom(document);

            if (yaml.containsKey("slug")) {
                slug = yaml.get("slug").stream().collect(Collectors.joining());
            }

            Article article = null;

            switch (method) {
            case POST:
                if (articleRepository.findBySlug(slug).isPresent()) {
                    throw new ConflictException(slug);
                }

                Author author =
                    authorRepository.findById(principal.getName()).get();

                article = new Article();
                article.setAuthor(author);
                article.setEmail(article.getAuthor().getEmail());
                article.setSlug(slug);
                article.setTitle(yaml.get("title")
                                 .stream().collect(Collectors.joining()));
                break;

            case PUT:
                article = articleRepository.findBySlug(slug).get();
                break;

            default:
                throw new MethodNotAllowedException(String.valueOf(method));
            }

            if (! article.getAuthor().getEmail().equals(principal.getName())) {
                throw new ForbiddenException(slug);
            }

            article.setMarkdown(markdown);
            article.getAttachments().clear();

            if (zip != null) {
                for (ZipArchiveEntry entry :
                         Collections.list(zip.getEntries())) {
                    if ((! entry.equals(source)) && (! entry.isDirectory())) {
                        String path = map.get(entry.getName());
                        byte[] content =
                            IOUtils.toByteArray(zip.getInputStream(entry));

                        Attachment attachment = new Attachment();

                        attachment.setArticle(article);
                        attachment.setPath(path);
                        attachment.setContent(content);

                        article.getAttachments().add(attachment);
                    }
                }
            }

            URI prefix = URI.create("/article/" + article.getSlug() + "/");
            CharSequence html = service.htmlRender(document, prefix);

            article.setHtml(html.toString());

            articleRepository.save(article);
        } finally {
            if (zip != null) {
                zip.close();
            }
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

    @Override
    public String toString() { return super.toString(); }

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

    @ResponseStatus(value = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public static class UnsupportedMediaTypeException extends RuntimeException {
        private static final long serialVersionUID = 4254521389130199048L;

        private UnsupportedMediaTypeException(String message) {
            super(message, null);
        }
    }
}
