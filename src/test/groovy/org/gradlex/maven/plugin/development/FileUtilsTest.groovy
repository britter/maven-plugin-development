/*
 * Copyright the GradleX team.
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

package org.gradlex.maven.plugin.development

import spock.lang.Specification
import spock.lang.TempDir

class FileUtilsTest extends Specification {

    @TempDir
    File tmp

    def "extracts file name extensions"() {
        given:
        def directory = new File(tmp, "directory").tap { mkdirs() }
        def fileWithoutExtension = new File(tmp, "file-without-extension").tap { it << "" }
        def fileWithExtension = new File(tmp, "some-file.txt").tap { it << "" }
        def fileWithMultipleDots = new File(tmp, "some.file.with.dot.txt").tap { it << "" }

        expect:
        !FileUtils.getExtension(directory).isPresent()
        !FileUtils.getExtension(fileWithoutExtension).isPresent()
        FileUtils.getExtension(fileWithExtension).get() == "txt"
        FileUtils.getExtension(fileWithMultipleDots).get() == "txt"
    }
}
