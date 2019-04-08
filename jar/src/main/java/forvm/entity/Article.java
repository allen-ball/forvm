/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package forvm.entity;

import ball.databind.JSONBean;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
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
import static javax.persistence.GenerationType.IDENTITY;

/**
 * {@bean.info}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Entity
@Table(catalog = "forvm", name = "articles")
@IdClass(Article.PK.class)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Article extends JSONBean {
    private static final long serialVersionUID = 3932015275027666636L;

    /** @serial */
    @Getter @Setter
    @Id @Column(length = 64, nullable = false)
    private String email = null;

    /** @serial */
    @Getter @Setter
    @Id @Column(length = 255, nullable = false, unique = true)
    private String slug = null;

    /** @serial */
    @Getter @Setter
    @Lob @Column(nullable = false)
    private String title = null;

    /** @serial */
    @Getter @Setter
    @Lob @Column(nullable = false)
    private String markdown = null;

    /** @serial */
    @Getter @Setter
    @Lob @Column(nullable = true)
    private String html = null;

    /** @serial */
    @Getter
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "email", insertable = false, updatable = false)
    private Author author = null;

    public void setAuthor(Author author) {
        this.author = author;

        if (author != null) {
            setEmail(author.getEmail());
        }
    }

    /** @serial */
    @Getter
    @OneToMany(mappedBy = "article", cascade = ALL)
    private List<Attachment> attachments = new ArrayList<>();

    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class PK extends JSONBean {
        private static final long serialVersionUID = 3053196702554146437L;

        /** @serial */ @Getter @Setter private String email = null;
        /** @serial */ @Getter @Setter private String slug = null;
    }
}
