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
import forvm.MarkdownConfiguration;
import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import org.apache.tools.ant.BuildException;
import org.commonmark.ext.front.matter.YamlFrontMatterVisitor;
import org.commonmark.node.Node;

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
    protected MarkdownConfiguration markdown = new MarkdownConfiguration();

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

                document = markdown.parser().parse(new String(bytes, UTF_8));

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

                log(markdown.renderer().render(document));
            } catch (BuildException exception) {
                throw exception;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                throw new BuildException(throwable);
            }
        }
    }
}
