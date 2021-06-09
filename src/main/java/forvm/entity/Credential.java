package forvm.entity;
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
 * {@link Credential} {@link Entity}.
 *
 * {@bean.info}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 */
@Entity
@Table(catalog = "forvm", name = "credentials")
@NoArgsConstructor @EqualsAndHashCode(callSuper = false)
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
