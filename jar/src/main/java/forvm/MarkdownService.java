/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package forvm;

import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.LinkResolver;
import com.vladsch.flexmark.html.LinkResolverFactory;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.html.renderer.LinkStatus;
import com.vladsch.flexmark.html.renderer.ResolvedLink;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.builder.Extension;
import com.vladsch.flexmark.util.options.MutableDataHolder;
import com.vladsch.flexmark.util.options.MutableDataSet;
import forvm.entity.Article;
import forvm.entity.Attachment;
import forvm.entity.Author;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Markdown {@link Service}
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Service
@NoArgsConstructor @ToString
public class MarkdownService {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Configured {@link Extension}s.
     * <p>{@include #EXTENSIONS}</p>
     */
    public static final List<Class<? extends Extension>> EXTENSIONS =
        Arrays.asList(com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension.class,
                      com.vladsch.flexmark.ext.admonition.AdmonitionExtension.class,
                      com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension.class,
                      com.vladsch.flexmark.ext.aside.AsideExtension.class,
                      com.vladsch.flexmark.ext.attributes.AttributesExtension.class,
                      com.vladsch.flexmark.ext.autolink.AutolinkExtension.class,
                      com.vladsch.flexmark.ext.definition.DefinitionExtension.class,
                      com.vladsch.flexmark.ext.emoji.EmojiExtension.class,
                      com.vladsch.flexmark.ext.enumerated.reference.EnumeratedReferenceExtension.class,
                      com.vladsch.flexmark.ext.footnotes.FootnoteExtension.class,
                      com.vladsch.flexmark.ext.gfm.issues.GfmIssuesExtension.class,
                      com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension.class,
                      com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension.class,
                      com.vladsch.flexmark.ext.gfm.users.GfmUsersExtension.class,
                      com.vladsch.flexmark.ext.gitlab.GitLabExtension.class,
                      com.vladsch.flexmark.ext.ins.InsExtension.class,
                      com.vladsch.flexmark.ext.media.tags.MediaTagsExtension.class,
                      com.vladsch.flexmark.ext.tables.TablesExtension.class,
                      com.vladsch.flexmark.ext.toc.TocExtension.class,
                      com.vladsch.flexmark.ext.typographic.TypographicExtension.class,
                      com.vladsch.flexmark.ext.wikilink.WikiLinkExtension.class,
                      com.vladsch.flexmark.ext.xwiki.macros.MacroExtension.class,
                      com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension.class,
                      com.vladsch.flexmark.ext.youtube.embedded.YouTubeLinkExtension.class,
                      com.vladsch.flexmark.superscript.SuperscriptExtension.class);

    private static final MutableDataHolder OPTIONS;

    static {
        ArrayList<Extension> extensions = new ArrayList<>();

        try {
            for (Class<? extends Extension> type : EXTENSIONS) {
                Extension extension =
                    (Extension) type.getMethod("create").invoke(null);

                extensions.add(extension);
            }
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }

        OPTIONS =
            new MutableDataSet()
            .setFrom(ParserEmulationProfile.MULTI_MARKDOWN)
            .set(Parser.EXTENSIONS, extensions)
            .set(HtmlRenderer.GENERATE_HEADER_ID, true);
    }

    private static final String README_MD = "README.md";
    private static final String SLASH = "/";
    private static final URI ROOT = URI.create(SLASH);

    @Autowired private Tika tika;

    /**
     * Method to compile the parameters into an {@link Author}.
     *
     * @param   name            The name of the input file.
     * @param   contents        The contents of the input file.
     * @param   slug            The {@link Author}'s slug.
     * @param   author          The {@link Author}'s entity to update.
     *
     * @throws  Exception       If any {@link Exception} is encountered.
     */
    public void compile(String name, byte[] contents,
                        String slug, Author author) throws Exception {
        ZipFile zip = null;

        try {
            String markdown = null;

            switch (String.valueOf(tika.detect(contents, name))) {
            case "application/zip":
                zip = new ZipFileImpl(name, contents);
                markdown = getEntryAsUTF8String(zip, README_MD);
                break;

            default:
                markdown = new String(contents, UTF_8);
                break;
            }

            Document document = parse(markdown);
            Map<String,List<String>> yaml = getYamlFrom(document);

            if (yaml.containsKey("email")) {
                String string =
                    yaml.get("email").stream().collect(Collectors.joining());

                author.setEmail(string);
            }

            author.setSlug(slug);

            if (yaml.containsKey("name")) {
                author.setName(yaml.get("name")
                               .stream().collect(Collectors.joining()));
            }

            author.setMarkdown(markdown);

            if (zip != null) {
                for (ZipArchiveEntry entry :
                         Collections.list(zip.getEntries())
                         .stream()
                         .filter(t -> (! t.isDirectory()))
                         .filter(t -> (! t.getName().equals(README_MD)))
                         .collect(Collectors.toList())) {
                    throw new IllegalArgumentException(entry.getName());
                }
            }

            author.setHtml(htmlRender(document, null).toString());
        } finally {
            if (zip != null) {
                zip.close();
            }
        }
    }

    private String getEntryAsUTF8String(ZipFile zip,
                                        String name) throws Exception {
        ZipArchiveEntry entry = zip.getEntry(name);
        Scanner scanner = new Scanner(zip.getInputStream(entry), UTF_8.name());

        return scanner.useDelimiter("\\A").next();

    }

    /**
     * Method to compile the parameters into an {@link Article}.
     *
     * @param   name            The name of the input file.
     * @param   contents        The contents of the input file.
     * @param   prefix          The {@link Article}'s path prefix.
     * @param   slug            The {@link Article}'s slug.
     * @param   article         The {@link Article}'s entity to update.
     *
     * @throws  Exception       If any {@link Exception} is encountered.
     */
    public void compile(String name, byte[] contents,
                        String prefix, String slug,
                        Article article) throws Exception {
        ZipFile zip = null;

        try {
            String markdown = null;

            switch (String.valueOf(tika.detect(contents, name))) {
            case "application/zip":
                zip = new ZipFileImpl(name, contents);
                markdown = getEntryAsUTF8String(zip, README_MD);
                break;

            default:
                markdown = new String(contents, UTF_8);
                break;
            }

            article.setSlug(slug);

            Document document = parse(markdown);
            Map<String,List<String>> yaml = getYamlFrom(document);
            String title =
                yaml.get("title").stream().collect(Collectors.joining());

            article.setTitle(title);
            article.setMarkdown(markdown);
            article.getAttachments().clear();

            if (zip != null) {
                for (ZipArchiveEntry entry :
                         Collections.list(zip.getEntries())
                         .stream()
                         .filter(t -> (! t.isDirectory()))
                         .filter(t -> (! t.getName().equals(README_MD)))
                         .collect(Collectors.toList())) {
                    String path =
                        ROOT.resolve(entry.getName()).normalize().getPath();
                    byte[] content =
                        IOUtils.toByteArray(zip.getInputStream(entry));
                    Attachment attachment = new Attachment();

                    attachment.setArticle(article);
                    attachment.setPath(path);
                    attachment.setContent(content);

                    article.getAttachments().add(attachment);
                }
            }

            String path = prefix + "/" + article.getSlug() + "/";
            URI uri = URI.create(path.replaceAll("[/]+", "/"));
            CharSequence html = htmlRender(document, uri);

            article.setHtml(html.toString());
        } finally {
            if (zip != null) {
                zip.close();
            }
        }
    }

    /**
     * See {@link Parser#parseReader(Reader)}.
     *
     * @param   markdown        The markdown {@link Reader}.
     *
     * @return  The parsed {@link Document}.
     *
     * @throws  IOException     If an I/O error is encountered.
     */
    public Document parse(Reader markdown) throws IOException {
        Parser.Builder builder = Parser.builder(OPTIONS);

        return builder.build().parseReader(markdown);
    }

    /**
     * See {@link Parser#parse(String)}.
     *
     * @param   markdown        The markdown {@link String}.
     *
     * @return  The parsed {@link Document}.
     */
    public Document parse(String markdown) {
        Parser.Builder builder = Parser.builder(OPTIONS);

        return builder.build().parse(markdown);
    }

    /**
     * See {@link HtmlRenderer#render(Node,Appendable)}.  Rewrites relative
     * and {@code FILE} URIs to match those that are uploaded as part of the
     * {@code markdown} document.
     *
     * @param   document        The {@link Document} to render.
     * @param   prefix          The prefix (as a {@link URI}).
     *
     * @return  The {@link CharSequence} representing the rendered
     *          {@code HTML}.
     */
    public CharSequence htmlRender(Document document, URI prefix) {
        StringBuilder html = new StringBuilder();
        HtmlRenderer.Builder builder = HtmlRenderer.builder(OPTIONS);

        if (prefix != null) {
            builder.linkResolverFactory(new LinkResolverFactoryImpl(prefix));
        }

        builder.build().render(document, html);

        return html;
    }

    /**
     * Method to get the YAML front matter from the document.
     *
     * @param   document        The {@link Document}.
     *
     * @return  The {@link Map} representing the parsed YAML.
     */
    public Map<String,List<String>> getYamlFrom(Document document) {
        AbstractYamlFrontMatterVisitor visitor =
            new AbstractYamlFrontMatterVisitor() { };

        visitor.visit(document);

        return visitor.getData();
    }

    private class ZipFileImpl extends ZipFile {
        public ZipFileImpl(String name, byte[] contents) throws IOException {
            super(new SeekableInMemoryByteChannel(contents),
                  name, UTF_8.name(), true);
        }

        @Override
        public String toString() { return super.toString(); }
    }

    private class LinkResolverFactoryImpl implements LinkResolverFactory {
        private final URI prefix;

        public LinkResolverFactoryImpl(URI prefix) {
            this.prefix = Objects.requireNonNull(prefix);
        }

        @Override
        public Set<Class<? extends LinkResolverFactory>> getAfterDependents() {
            return null;
        }

        @Override
        public Set<Class<? extends LinkResolverFactory>> getBeforeDependents() {
            return null;
        }

        @Override
        public boolean affectsGlobalScope() { return false; }

        @Override
        public LinkResolver create(LinkResolverContext context) {
            return new LinkResolverImpl(context, prefix);
        }

        @Override
        public String toString() { return super.toString(); }
    }

    private class LinkResolverImpl implements LinkResolver {
        private static final String FILE = "file";
        private static final String SLASH = "/";

        private final URI prefix;

        public LinkResolverImpl(LinkResolverContext context, URI prefix) {
            this.prefix = Objects.requireNonNull(prefix);
        }

        @Override
        public ResolvedLink resolveLink(Node node,
                                        LinkResolverContext context,
                                        ResolvedLink link) {
            URI uri = URI.create(link.getUrl()).normalize();

            if ((! uri.isAbsolute()) || FILE.equals(uri.getScheme())) {
                if (! uri.getPath().startsWith(SLASH)) {
                    link =
                        link
                        .withStatus(LinkStatus.VALID)
                        .withUrl(prefix.resolve(uri).toString());
                }
            }

            return link;
        }

        @Override
        public String toString() { return super.toString(); }
    }
}