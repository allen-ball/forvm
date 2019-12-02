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
import lombok.extern.log4j.Log4j2;

/**
 * Backing bean for "change password" form
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
public class ChangePasswordForm {
    private String username;
    private String password;
    private String newPassword;
    private String repeatPassword;
}
