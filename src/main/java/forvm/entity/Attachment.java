package forvm.entity;
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
 * {@link Attachment} {@link Entity}.
 *
 * {@bean.info}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@Entity
@Table(catalog = "forvm", name = "attachments")
@IdClass(Attachment.PK.class)
@NoArgsConstructor @EqualsAndHashCode(callSuper = false)
public class Attachment extends JSONBean {
    private static final long serialVersionUID = -4239090769692766154L;

    /** @serial */
    @Id @Column(length = 64, nullable = false)
    @Getter @Setter
    private String email = null;

    /** @serial */
    @Id @Column(length = 255, nullable = false)
    @Getter @Setter
    private String slug = null;

    /** @serial */
    @Id @Column(length = 255, nullable = false)
    @Getter @Setter
    private String path = null;

    /** @serial */
    @Lob @Column(nullable = false)
    @Getter @Setter
    private byte[] content = null;

    /** @serial */
    @ManyToOne(fetch = LAZY)
    @JoinColumns({
        @JoinColumn(name = "email", insertable = false, updatable = false),
            @JoinColumn(name = "slug", insertable = false, updatable = false)
    })
    @Getter
    private Article article = null;

    public void setArticle(Article article) {
        this.article = article;

        if (article != null) {
            setEmail(article.getEmail());
            setSlug(article.getSlug());
        }
    }

    @NoArgsConstructor @EqualsAndHashCode(callSuper = false)
    public static class PK extends JSONBean {
        private static final long serialVersionUID = 805706754108867472L;

        /** @serial */ @Getter @Setter private String email = null;
        /** @serial */ @Getter @Setter private String slug = null;
        /** @serial */ @Getter @Setter private String path = null;
    }
}
