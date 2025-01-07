/*
 * Copyright 2022 the GradleX team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradlex.maven.plugin.development.issues

import org.gradlex.maven.plugin.development.AbstractPluginFuncTest
import spock.lang.Issue

@Issue("https://github.com/gradlex-org/maven-plugin-development/pull/9")
class Issue9FuncTest extends AbstractPluginFuncTest {

    def "works even if weird enums are present"() {
        given:
        file("src/main/java/TypedEnum.java") << """
        import java.util.List;
        public enum TypedEnum {
            ENUM_VALUE {
                private <T> List<T> genericMethod(List<T> list) {
                    return list;
                }
            }
        }
        """

        expect:
        run("generateMavenPluginDescriptor")
    }
}
