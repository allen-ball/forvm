package forvm.ant.taskdefs;
/*-
 * ##########################################################################
 * forvm Blog Publishing Platform
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2018 - 2020 Allen D. Ball
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ##########################################################################
 */
import ball.swing.table.MapTableModel;
import ball.util.ant.taskdefs.AnnotatedAntTask;
import ball.util.ant.taskdefs.AntTask;
import ball.util.ant.taskdefs.ClasspathDelegateAntTask;
import ball.util.ant.taskdefs.ConfigurableAntTask;
import ball.util.ant.taskdefs.NotNull;
import com.vladsch.flexmark.util.ast.Document;
import forvm.MarkdownService;
import java.io.File;
import java.nio.file.Files;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.ClasspathUtils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static lombok.AccessLevel.PROTECTED;

/**
 * Abstract base class for {@link.uri http://ant.apache.org/ Ant}
 * {@link Task}s to parse and/or render markdown.
 *
 * {@ant.task}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@NoArgsConstructor(access = PROTECTED)
public abstract class MarkdownTask extends Task
                                   implements AnnotatedAntTask,
                                              ClasspathDelegateAntTask,
                                              ConfigurableAntTask {
    @Getter @Setter @Accessors(chain = true, fluent = true)
    private ClasspathUtils.Delegate delegate = null;
    protected MarkdownService service = new MarkdownService();

    @Override
    public void init() throws BuildException {
        super.init();
        ClasspathDelegateAntTask.super.init();
        ConfigurableAntTask.super.init();
    }

    @Override
    public void execute() throws BuildException {
        super.execute();
        AnnotatedAntTask.super.execute();
    }

    /**
     * {@link.uri http://ant.apache.org/ Ant}
     * {@link org.apache.tools.ant.Task} to parse markdown.
     *
     * {@ant.task}
     */
    @AntTask("markdown-parse")
    @NoArgsConstructor @ToString
    public static class Parse extends MarkdownTask {
        @NotNull @Getter @Setter
        private File file = null;
        protected Document document = null;

        @Override
        public void execute() throws BuildException {
            super.execute();

            try {
                var bytes = Files.readAllBytes(getFile().toPath());

                document = service.parse(new String(bytes, UTF_8));

                var yaml = service.getYamlFrom(document);

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
     * {@ant.task}
     */
    @AntTask("markdown-render")
    @NoArgsConstructor @ToString
    public static class Render extends Parse {
        @Override
        public void execute() throws BuildException {
            super.execute();

            try {
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
