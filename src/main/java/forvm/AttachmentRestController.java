package forvm;
/*-
 * ##########################################################################
 * forvm Blog Publishing Platform
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2018 - 2021 Allen D. Ball
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
import forvm.entity.Article;
import forvm.repository.ArticleRepository;
import forvm.repository.AttachmentRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.servlet.http.HttpSession;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Attachment {@link RestController} implementation.
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@RestController
@NoArgsConstructor @ToString @Log4j2
public class AttachmentRestController {
    @Autowired private ArticleRepository articleRepository = null;
    @Autowired private AttachmentRepository attachmentRepository = null;

    @RequestMapping(method = { GET }, value = { "/article/{slug}/{name:.+}" })
    @PreAuthorize("permitAll()")
    public byte[] get(@PathVariable String slug,
                      @PathVariable String name) throws Exception {
        var path = "/" + name;
        var attachment =
            articleRepository.findBySlug(slug)
            .flatMap(t -> attachmentRepository.findByArticleAndPath(t, path));

        return attachment.get().getContent();
    }

    @RequestMapping(method = { GET }, value = { "/preview/{slug}/{name:.+}" })
    @PreAuthorize("hasAuthority('AUTHOR')")
    public byte[] get(HttpSession session,
                      @PathVariable String slug,
                      @PathVariable String name) throws Exception {
        var path = "/" + name;
        var attachment =
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
