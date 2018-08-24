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
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * {@bean.info}
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(catalog = "forvm", name = "tags")
public class Tag extends JSONBean {
    private static final long serialVersionUID = -4232542875901854740L;

    @Getter @Setter
    @Id @Column(length = 32, nullable = false, unique = true)
    private String name = null;

    @Getter @Setter
    @Column(length = 255, nullable = false, unique = true)
    private String slug = null;
}
