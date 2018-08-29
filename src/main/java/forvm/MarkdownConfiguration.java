/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm;

import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Markdown {@link Parser} and {@link HtmlRenderer} {@link Configuration}
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@Configuration
public class MarkdownConfiguration {
    private static final Logger LOGGER = LogManager.getLogger();

    private static List<Extension> EXTENSIONS =
        Arrays.asList(AutolinkExtension.create(),
                      StrikethroughExtension.create(),
                      TablesExtension.create(),
                      HeadingAnchorExtension.create(),
                      InsExtension.create(),
                      YamlFrontMatterExtension.create());

    /**
     * Sole constructor.
     */
    public MarkdownConfiguration () { }

    @Bean
    public Parser parser() {
        return Parser.builder().extensions(EXTENSIONS).build();
    }

    @Bean
    public HtmlRenderer renderer() {
        return HtmlRenderer.builder().extensions(EXTENSIONS).build();
    }

    @Override
    public String toString() { return super.toString(); }
}
