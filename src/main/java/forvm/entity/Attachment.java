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
    private static final long serialVersionUID = -7858728595658356959L;

    @Getter @Setter
    @Id @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post", referencedColumnName = "id")
    private Post post = null;

    @Getter @Setter
    @Id @Column(length = 255, nullable = false)
    private String path = null;

    @Getter @Setter
    @Lob @Column(nullable = false)
    private byte[] content = null;

    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    public static class PK extends JSONBean {
        private static final long serialVersionUID = 7968965627740206684L;

        @Getter @Setter private long post = -1;
        @Getter @Setter private String path = null;
    }
}
