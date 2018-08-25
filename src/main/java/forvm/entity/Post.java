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
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private static final long serialVersionUID = 6458730758489386403L;

    @Getter @Setter
    @Id @GeneratedValue(strategy = IDENTITY)
    private long id = -1;

    @Getter @Setter
    @Column(length = 255, nullable = false, unique = true)
    private String slug = null;

    @Getter @Setter
    @Lob @Column(nullable = false)
    private String markdown = null;
}
