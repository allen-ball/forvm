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
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
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
@Table(catalog = "forvm", name = "credentials")
public class Credential extends JSONBean {
    private static final long serialVersionUID = -4744967204003834531L;

    /** @serial */
    @Getter @Setter
    @Id @Column(length = 64, nullable = false, unique = true)
    @NotBlank @Email
    private String email = null;

    /** @serial */
    @Getter @Setter
    @Lob @Column(nullable = false)
    @NotBlank
    private String password = null;
}
