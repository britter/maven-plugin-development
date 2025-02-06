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

package org.gradlex.maven.plugin.development;

import java.io.File;
import java.util.Optional;

final class FileUtils {

    static Optional<String> getExtension(File file) {
        if (!file.isFile()) {
            return Optional.empty();
        }
        String[] fileNameSegments = file.getName().split("\\.");
        if (fileNameSegments.length == 1) {
            return Optional.empty();
        }
        return Optional.of(fileNameSegments[fileNameSegments.length - 1]);
    }

    private FileUtils() {
    }
}
