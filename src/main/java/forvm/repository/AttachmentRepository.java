package forvm.repository;
/*-
 * ##########################################################################
 * forvm Blog Publishing Platform
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
import forvm.entity.Attachment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link Attachment} {@link JpaRepository}.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 */
@Repository
@Transactional(readOnly = true)
public interface AttachmentRepository
                 extends JpaRepository<Attachment,Attachment.PK> {
    public Optional<Attachment> findByArticleAndPath(Article article, String path);
}
