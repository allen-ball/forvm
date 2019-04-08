/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
@ToString
public class ChangePasswordForm {
    private static final Logger LOGGER = LogManager.getLogger();

    private String username;
    private String password;
    private String newPassword;
    private String repeatPassword;
}
