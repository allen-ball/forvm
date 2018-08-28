/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm.entity;

import ball.databind.JSONBean;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

/**
 * {@bean.info}
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(catalog = "forvm", name = "posts")
public class Post extends JSONBean {
    private static final long serialVersionUID = 18952798034523858L;

    @Getter @Setter
    @Id @GeneratedValue(strategy = IDENTITY)
    private long id = -1;

    @Getter @Setter
    @ManyToOne(fetch = LAZY) @JoinColumn(name = "email")
    private Author author = null;

    @Getter @Setter
    @Column(length = 255, nullable = false, unique = true)
    private String slug = null;

    @Getter @Setter
    @Lob @Column(nullable = false)
    private String markdown = null;
}
