/*
 * $Id$
 *
 * Copyright 2018 - 2020 Allen D. Ball.  All rights reserved.
 */
package forvm.repository;

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
 * @version $Revision$
 */
@Repository
@Transactional(readOnly = true)
public interface AttachmentRepository
                 extends JpaRepository<Attachment,Attachment.PK> {
    public Optional<Attachment> findByArticleAndPath(Article article,
                                                     String path);
}
