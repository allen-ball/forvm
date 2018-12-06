/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm.entity;

import ball.databind.JSONBean;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static javax.persistence.FetchType.LAZY;

/**
 * {@bean.info}
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(catalog = "forvm", name = "attachments")
@IdClass(Attachment.PK.class)
public class Attachment extends JSONBean {
    private static final long serialVersionUID = -4239090769692766154L;

    @Getter @Setter
    @Id @Column(length = 64, nullable = false)
    private String email = null;

    @Getter @Setter
    @Id @Column(length = 255, nullable = false)
    private String slug = null;

    @Getter @Setter
    @Id @Column(length = 255, nullable = false)
    private String path = null;

    @Getter @Setter
    @Lob @Column(nullable = false)
    private byte[] content = null;

    @Getter
    @ManyToOne(fetch = LAZY)
    @JoinColumns({
        @JoinColumn(name = "email", insertable = false, updatable = false),
            @JoinColumn(name = "slug", insertable = false, updatable = false)
    })
    private Article article = null;

    public void setArticle(Article article) {
        this.article = article;

        if (article != null) {
            setEmail(article.getEmail());
            setSlug(article.getSlug());
        }
    }

    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class PK extends JSONBean {
        private static final long serialVersionUID = 805706754108867472L;

        @Getter @Setter private String email = null;
        @Getter @Setter private String slug = null;
        @Getter @Setter private String path = null;
    }
}
