/*
 * $Id$
 *
 * Copyright 2018 - 2020 Allen D. Ball.  All rights reserved.
 */
package forvm.entity;

import ball.databind.JSONBean;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.LAZY;

/**
 * {@link Article} {@link Entity}.
 *
 * {@bean.info}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Entity
@Table(catalog = "forvm", name = "articles")
@IdClass(Article.PK.class)
@NoArgsConstructor @EqualsAndHashCode(callSuper = false)
public class Article extends JSONBean {
    private static final long serialVersionUID = 3932015275027666636L;

    /** @serial */
    @Id @Column(length = 64, nullable = false)
    @Getter @Setter
    private String email = null;

    /** @serial */
    @Id @Column(length = 255, nullable = false, unique = true)
    @Getter @Setter
    private String slug = null;

    /** @serial */
    @Lob @Column(nullable = false)
    @Getter @Setter
    private String title = null;

    /** @serial */
    @Lob @Column(nullable = false)
    @Getter @Setter
    private String markdown = null;

    /** @serial */
    @Lob @Column(nullable = true)
    @Getter @Setter
    private String html = null;

    /** @serial */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "email", insertable = false, updatable = false)
    @Getter
    private Author author = null;

    public void setAuthor(Author author) {
        this.author = author;

        if (author != null) {
            setEmail(author.getEmail());
        }
    }

    /** @serial */
    @OneToMany(mappedBy = "article", cascade = ALL)
    @Getter
    private List<Attachment> attachments = new ArrayList<>();

    @NoArgsConstructor @EqualsAndHashCode(callSuper = false)
    public static class PK extends JSONBean {
        private static final long serialVersionUID = 3053196702554146437L;

        /** @serial */ @Getter @Setter private String email = null;
        /** @serial */ @Getter @Setter private String slug = null;
    }
}
