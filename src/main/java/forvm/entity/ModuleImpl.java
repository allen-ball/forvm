package forvm.entity;
/*-
 * ##########################################################################
 * forvm Blog Publishing Platform
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2018 - 2020 Allen D. Ball
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ##########################################################################
 */
import ball.annotation.ServiceProviderFor;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.ToString;

/**
 * {@link Module} service provider for {@link forvm.entity}.
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 * @version $Revision$
 */
@ServiceProviderFor({ Module.class })
@ToString
public class ModuleImpl extends SimpleModule {

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
}
