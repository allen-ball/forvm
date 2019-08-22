/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package forvm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;

/**
 * Backing bean for "login" form
 *
 * {@bean.info}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@NoArgsConstructor
@Getter @Setter
@EqualsAndHashCode(callSuper = false)
@ToString @Log4j2
public class LoginForm {
    private String username;
    private String password;
}
