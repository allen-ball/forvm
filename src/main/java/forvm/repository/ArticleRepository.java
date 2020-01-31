/*
 * $Id$
 *
 * Copyright 2018 - 2020 Allen D. Ball.  All rights reserved.
 */
package forvm.repository;

import forvm.entity.Article;
import forvm.entity.Author;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link Article} {@link JpaRepository}.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Repository
@Transactional(readOnly = true)
public interface ArticleRepository extends JpaRepository<Article,Article.PK> {
    public Optional<Article> findBySlug(String slug);
    public Page<Article> findByAuthor(Pageable pageable, Author author);
}
