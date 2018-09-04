/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm.entity;

import ball.databind.JSONBean;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(catalog = "forvm", name = "posts")
public class Post extends JSONBean {
    private static final long serialVersionUID = -3653815293462153851L;

    @Getter @Setter
    @Id @GeneratedValue(strategy = IDENTITY)
    private long id = -1;

    @Getter @Setter
    @Column(length = 255, nullable = false, unique = true)
    private String slug = null;

    @Getter @Setter
    @ManyToOne(fetch = LAZY) @JoinColumn(name = "author")
    private Author author = null;

    @Getter @Setter
    @Lob @Column(nullable = false)
    private String markdown = null;

    @Getter @Setter
    @Lob @Column(nullable = true)
    private String html = null;

    @OneToMany(mappedBy = "post", cascade = ALL)
    private List<Attachment> attachments = new ArrayList<>();
}
