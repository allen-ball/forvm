/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm;

import com.vladsch.flexmark.Extension;
import com.vladsch.flexmark.ast.Document;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.LinkResolver;
import com.vladsch.flexmark.html.LinkResolverFactory;
import com.vladsch.flexmark.html.renderer.LinkResolverContext;
import com.vladsch.flexmark.html.renderer.LinkStatus;
import com.vladsch.flexmark.html.renderer.ResolvedLink;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.parser.ParserEmulationProfile;
import com.vladsch.flexmark.util.options.MutableDataHolder;
import com.vladsch.flexmark.util.options.MutableDataSet;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

/**
 * Markdown {@link Service}
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@Service
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

    /**
     * Sole constructor.
     */
    public MarkdownService() { }

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

    @Override
    public String toString() { return super.toString(); }

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
