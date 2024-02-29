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

package de.benediktritter.maven.plugin.development.documentation

import org.gradle.exemplar.executor.CliCommandExecutor
import org.gradle.exemplar.executor.CommandExecutor
import org.gradle.exemplar.executor.ExecutionMetadata
import org.gradle.exemplar.executor.GradleRunnerCommandExecutor
import org.gradle.exemplar.model.Command
import org.gradle.exemplar.test.runner.SamplesRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.runners.model.InitializationError

import javax.annotation.Nullable

class PluginUnderTestAwareGradleSamplesRunner extends SamplesRunner {
    private static final String GRADLE_EXECUTABLE = "gradle";
    @Rule
    public TemporaryFolder tempGradleUserHomeDir = new TemporaryFolder();
    private File customGradleInstallation = null;

    PluginUnderTestAwareGradleSamplesRunner(Class<?> testClass) throws InitializationError {
        super(testClass)
    }

    @Override
    protected CommandExecutor selectExecutor(ExecutionMetadata executionMetadata, File workingDir, Command command) {
        boolean expectFailure = command.isExpectFailure();
        if (command.getExecutable().equals(GRADLE_EXECUTABLE)) {
            return new PluginUnderTestAwareGradleRunnerCommandExecutor(workingDir, customGradleInstallation, expectFailure);
        }
        return new CliCommandExecutor(workingDir);
    }

    @Nullable
    @Override
    protected File getImplicitSamplesRootDir() {
        String gradleHomeDir = getCustomGradleInstallationFromSystemProperty();
        if (System.getProperty("integTest.samplesdir") != null) {
            String samplesRootProperty = System.getProperty("integTest.samplesdir", gradleHomeDir + "/samples");
            return new File(samplesRootProperty);
        } else if (customGradleInstallation != null) {
            return new File(customGradleInstallation, "samples");
        } else {
            return null;
        }
    }

    @Nullable
    private String getCustomGradleInstallationFromSystemProperty() {
        // Allow Gradle installation and samples root dir to be set from a system property
        // This is to allow Gradle to test Gradle installations during integration testing
        final String gradleHomeDirProperty = System.getProperty("integTest.gradleHomeDir");
        if (gradleHomeDirProperty != null) {
            File customGradleInstallationDir = new File(gradleHomeDirProperty);
            if (customGradleInstallationDir.exists()) {
                this.customGradleInstallation = customGradleInstallationDir;
            } else {
                throw new RuntimeException(String.format("Custom Gradle installation dir at %s was not found", gradleHomeDirProperty));
            }
        }
        return gradleHomeDirProperty;
    }
}
