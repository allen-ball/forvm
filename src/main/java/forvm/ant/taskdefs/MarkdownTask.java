/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package forvm.ant.taskdefs;

import ball.swing.table.MapTableModel;
import ball.util.ant.taskdefs.AbstractClasspathTask;
import ball.util.ant.taskdefs.AntTask;
import ball.util.ant.taskdefs.ConfigurableAntTask;
import ball.util.ant.taskdefs.NotNull;
import com.vladsch.flexmark.ast.Document;
import forvm.MarkdownService;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.tools.ant.BuildException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PROTECTED;

/**
 * Abstract base class for {@link.uri http://ant.apache.org/ Ant}
 * {@link org.apache.tools.ant.Task}s to parse and/or render markdown.
 *
 * {@bean.info}
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@NoArgsConstructor(access = PROTECTED)
public abstract class MarkdownTask extends AbstractClasspathTask
                                   implements ConfigurableAntTask {
    protected MarkdownService service = new MarkdownService();

    /**
     * {@link.uri http://ant.apache.org/ Ant}
     * {@link org.apache.tools.ant.Task} to parse markdown.
     *
     * {@bean.info}
     */
    @AntTask("markdown-parse")
    @NoArgsConstructor @ToString
    public static class Parse extends MarkdownTask {
        @NotNull @Getter @Setter
        private File file = null;
        protected Document document = null;

        @Override
        public void execute() throws BuildException {
            try {
                super.execute();

                byte[] bytes = Files.readAllBytes(getFile().toPath());

                document = service.parse(new String(bytes, UTF_8));

                Map<String,List<String>> yaml = service.getYamlFrom(document);

                log(new MapTableModel(yaml));
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
    @NoArgsConstructor @ToString
    public static class Render extends Parse {
        @Override
        public void execute() throws BuildException {
            try {
                super.execute();

                log(service.htmlRender(document, null).toString());
            } catch (BuildException exception) {
                throw exception;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                throw new BuildException(throwable);
            }
        }
    }
}
