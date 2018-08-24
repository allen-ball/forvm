/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm.ant.taskdefs;

import ball.swing.table.MapTableModel;
import ball.util.MapUtil;
import ball.util.PropertiesImpl;
import ball.util.ant.taskdefs.AbstractClasspathTask;
import ball.util.ant.taskdefs.AntTask;
import ball.util.ant.taskdefs.NotNull;
import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Abstract base class for {@link.uri http://ant.apache.org/ Ant}
 * {@link org.apache.tools.ant.Task}s to parse and/or render markdown.
 *
 * {@bean.info}
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
public abstract class MarkdownTask extends AbstractClasspathTask {
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
    protected MarkdownTask() {
        super();
    }

    @Override
    public void init() throws BuildException {
        super.init();

        PropertiesImpl properties = new PropertiesImpl();

        MapUtil.copy(getProject().getProperties(), properties);
        properties.configure(this);
    }

    /**
     * {@link.uri http://ant.apache.org/ Ant}
     * {@link org.apache.tools.ant.Task} to parse markdown.
     *
     * {@bean.info}
     */
    @AntTask("markdown-parse")
    public static class Parse extends MarkdownTask {
        private File file = null;
        protected Parser parser = null;
        protected Node document = null;

        /**
         * Sole constructor.
         */
        public Parse() { super(); }

        @NotNull
        public File getFile() { return file; }
        public void setFile(File file) { this.file = file; }
        public void setFile(String string) { setFile(new File(string)); }

        @Override
        public void execute() throws BuildException {
            try {
                super.execute();

                byte[] bytes = Files.readAllBytes(getFile().toPath());

                parser = Parser.builder().extensions(EXTENSIONS).build();
                document = parser.parse(new String(bytes, UTF_8));

                YamlFrontMatterVisitor visitor = new YamlFrontMatterVisitor();

                document.accept(visitor);

                log(new MapTableModel(visitor.getData()));
            } catch (BuildException exception) {
                throw exception;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                throw new BuildException(throwable);
            }
        }
    }

    /**
     * {@link.uri http://ant.apache.org/ Ant}
     * {@link org.apache.tools.ant.Task} to render markdown to HTML.
     *
     * {@bean.info}
     */
    @AntTask("markdown-render")
    public static class Render extends Parse {

        /**
         * Sole constructor.
         */
        public Render() { super(); }

        @Override
        public void execute() throws BuildException {
            try {
                super.execute();

                HtmlRenderer renderer =
                    HtmlRenderer.builder().extensions(EXTENSIONS).build();

                log(renderer.render(document));
            } catch (BuildException exception) {
                throw exception;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                throw new BuildException(throwable);
            }
        }
    }
}
