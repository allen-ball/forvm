/*
 * $Id$
 *
 * Copyright 2018, 2019 Allen D. Ball.  All rights reserved.
 */
package forvm.entity;

import ball.databind.JSONBeanTypeMap;
import lombok.NoArgsConstructor;

/**
 * {@link JSONBeanTypeMap} instance.
 * <p>{@include #INSTANCE}</p>
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
@NoArgsConstructor
public class MAP extends JSONBeanTypeMap {
    private static final long serialVersionUID = -3252247011324498421L;

    /**
     * An instance of {@link MAP}.
     */
    public static final MAP INSTANCE = new MAP();
}
