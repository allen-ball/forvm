package forvm;
/*-
 * ##########################################################################
 * forvm Blog Publishing Platform
 * %%
 * Copyright (C) 2018 - 2021 Allen D. Ball
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
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * {@link ControllerAdvice} implementation.
 *
 * {@injected.fields}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 */
@ControllerAdvice
@NoArgsConstructor @ToString @Log4j2
public class ControllerAdviceImpl {
    @Autowired private ApplicationContext context = null;
    private List<ClientRegistration> oauth2 = null;

    @PostConstruct
    public void init() { }

    @PreDestroy
    public void destroy() { }

    @ModelAttribute("oauth2")
    public List<ClientRegistration> oauth2() {
        if (oauth2 == null) {
            oauth2 = new ArrayList<>();

            try {
                var repository = context.getBean(ClientRegistrationRepository.class);

                if (repository != null) {
                    var type =
                        ResolvableType.forInstance(repository)
                        .as(Iterable.class);

                    if (type != ResolvableType.NONE
                        && ClientRegistration.class.isAssignableFrom(type.resolveGenerics()[0])) {
                        ((Iterable<?>) repository)
                            .forEach(t -> oauth2.add((ClientRegistration) t));
                    }
                }
            } catch (Exception exception) {
            }
        }

        return oauth2;
    }

    @ModelAttribute("isPasswordAuthenticated")
    public boolean isPasswordAuthenticated(Principal principal) {
        return principal instanceof UsernamePasswordAuthenticationToken;
    }
}
