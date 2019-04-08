/*
 * $Id$
 *
 * Copyright 2018 Allen D. Ball.  All rights reserved.
 */
package forvm.entity;

import ball.annotation.ServiceProviderFor;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * {@link Module} service provider for {@link forvm.entity}.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@ServiceProviderFor({ Module.class })
public class ModuleImpl extends SimpleModule {
    private static final long serialVersionUID = 7298172921762166185L;

    /**
     * Sole constructor.
     */
    public ModuleImpl() {
        super(ModuleImpl.class.getPackage().getName());
    }

    @Override
    public void setupModule(Module.SetupContext context) {
        super.setupModule(context);

        context.addBeanDeserializerModifier(MAP.INSTANCE.getBeanDeserializerModifier());
    }

    @Override
    public String toString() { return super.toString(); }
}
