package forvm.entity;
/*-
 * ##########################################################################
 * forvm Blog Publishing Platform
 * %%
 * Copyright (C) 2018 - 2022 Allen D. Ball
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
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static javax.persistence.CascadeType.ALL;

/**
 * {@link Author} {@link Entity}.
 *
 * {@bean.info}
 *
 * @author {@link.uri mailto:ball@hcf.dev Allen D. Ball}
 */
@Entity
@Table(catalog = "forvm", name = "authors")
@NoArgsConstructor @EqualsAndHashCode(callSuper = false)
public class Author extends JSONBean {
    private static final long serialVersionUID = -4572928690438354380L;

    /** @serial */
    @Getter @Setter
    @Id @Column(length = 64, nullable = false, unique = true)
    private String email = null;

    /** @serial */
    @Getter @Setter
    @Column(length = 255, nullable = false, unique = true)
    private String slug = null;

    /** @serial */
    @Getter @Setter
    @Lob @Column(nullable = true)
    private String name = null;

    /** @serial */
    @Getter @Setter
    @Lob @Column(nullable = false)
    private String markdown = null;

    /** @serial */
    @Getter @Setter
    @Lob @Column(nullable = true)
    private String html = null;

    /** @serial */
    @OneToMany(mappedBy = "author", cascade = ALL)
    private List<Article> articles = new ArrayList<>();
}
