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

import org.gradle.exemplar.executor.CommandExecutor
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner

import javax.annotation.Nullable

class PluginUnderTestAwareGradleRunnerCommandExecutor extends CommandExecutor {
    private final File workingDir;
    private final File customGradleInstallation;
    private final boolean expectFailure;

    public PluginUnderTestAwareGradleRunnerCommandExecutor(File workingDir, @Nullable File customGradleInstallation, boolean expectFailure) {
        this.workingDir = workingDir;
        this.customGradleInstallation = customGradleInstallation;
        this.expectFailure = expectFailure;
    }

    @Override
    protected int run(String executable, List<String> args, List<String> flags, OutputStream output) {
        List<String> allArguments = new ArrayList<>(args);
        allArguments.addAll(flags);

        println(System.getProperty("java.class.path"))

        GradleRunner gradleRunner = GradleRunner.create()
                .withProjectDir(workingDir)
                .withArguments(allArguments)
                .withPluginClasspath()
                .forwardOutput();

        if (customGradleInstallation != null) {
            gradleRunner.withGradleInstallation(customGradleInstallation);
        }

        output.withWriter {
            BuildResult buildResult;
            if (expectFailure) {
                buildResult = gradleRunner.buildAndFail();
            } else {
                buildResult = gradleRunner.build();
            }
            it.write(buildResult.getOutput());
            it.close();
            return expectFailure ? 1 : 0;
        }
    }
}
