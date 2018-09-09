/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm;

import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Bootstrap UI {@link org.springframework.stereotype.Controller} abstract
 * base class
 *
 * @author {@link.uri mailto:ball@iprotium.com Allen D. Ball}
 * @version $Revision$
 */
public abstract class BootstrapUI {

    /**
     * View name for this template.
     */
    protected static final String VIEW = BootstrapUI.class.getSimpleName();

    /**
     * Sole constructor.
     */
    protected BootstrapUI() { }

    @ModelAttribute("css")
    public String[] css() { return new String[] { }; }

    @ModelAttribute("js")
    public String[] js() { return new String[] { }; }

    @ModelAttribute("brand")
    public String brand() { return null; }

    @ModelAttribute("template")
    public abstract String template();

    @Override
    public String toString() { return super.toString(); }
}
