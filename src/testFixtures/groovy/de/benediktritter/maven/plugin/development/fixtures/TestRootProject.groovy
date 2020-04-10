/*
 * Copyright 2020 Benedikt Ritter
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

package de.benediktritter.maven.plugin.development.fixtures

import org.junit.rules.ExternalResource
import org.junit.rules.TemporaryFolder

class TestRootProject extends ExternalResource implements TestProject {

    private TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Lazy DescriptorFile pluginDescriptor = pluginDescriptor()

    @Lazy DescriptorFile helpDescriptor = helpDescriptor()

    @Override
    void before() {
        temporaryFolder.create()
    }

    @Override
    void after() {
        temporaryFolder.delete()
    }

    File getProjectDir() {
        temporaryFolder.root
    }

    TestProject subproject(String projectName, @DelegatesTo(TestProject) Closure<TestProject> configureProject) {
        settingsFile << """
            include '$projectName'
        """
        def sub = new TestSubproject(dir(projectName))
        configureProject.delegate = sub
        configureProject.call(sub)
        sub
    }
}
