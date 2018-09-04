/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm.entity;

import ball.databind.JSONBean;
import ball.persistence.embeddable.PersonName;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static javax.persistence.CascadeType.ALL;

/**
 * {@bean.info}
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(catalog = "forvm", name = "authors")
public class Author extends JSONBean {
    private static final long serialVersionUID = -5234636709100963538L;

    @Getter @Setter
    @Id @Column(length = 64, nullable = false, unique = true)
    private String email = null;

    @Getter @Setter
    @Column(length = 255, nullable = false, unique = true)
    private String slug = null;

    @Getter
    @Embedded
    private PersonName name = null;

    @Getter @Setter
    @Lob @Column(nullable = false)
    private String markdown = null;

    @Getter @Setter
    @Lob @Column(nullable = true)
    private String html = null;

    @OneToMany(mappedBy = "author", cascade = ALL)
    private List<Post> posts = new ArrayList<>();
}
