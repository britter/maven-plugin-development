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

package org.gradlex.maven.plugin.development.fixtures

import java.util.zip.ZipFile

class FileExtensions {

    static boolean contains(File self, String path) {
        if (self.name.endsWith(".jar") || self.name.endsWith(".zip")) {
            def zip = new ZipFile(self)
            return zip.withCloseable {
                def entries = zip.entries()
                while(entries.hasMoreElements()) {
                    def entry = entries.nextElement()
                    if (entry.name == path) {
                        return true
                    }
                }
                return false
            }
        }
        false
    }

    static boolean contains(File self, DescriptorFile descriptor) {
        contains(self, descriptor.path)
    }

    static void replace(File self, String text, String replacement) {
        def oldText = self.text
        self.text = oldText.replace(text, replacement)
    }
}
