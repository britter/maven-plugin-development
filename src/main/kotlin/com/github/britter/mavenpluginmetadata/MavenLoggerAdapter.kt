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

package com.github.britter.mavenpluginmetadata

import org.gradle.api.logging.Logger

class MavenLoggerAdapter(private val delegate: Logger) : org.codehaus.plexus.logging.Logger, org.apache.maven.plugin.logging.Log {

    override fun getName(): String {
        return delegate.name
    }

    override fun getThreshold(): Int {
        // not supported
        return 0
    }

    override fun setThreshold(threshold: Int) {
        // not supported
    }

    override fun getChildLogger(name: String?): org.codehaus.plexus.logging.Logger {
        // not supported
        return this
    }

    override fun isDebugEnabled(): Boolean {
        return delegate.isDebugEnabled
    }

    override fun debug(content: CharSequence?) {
        debug(content.toString())
    }

    override fun debug(content: CharSequence?, error: Throwable?) {
        debug(content.toString(), error)
    }

    override fun debug(error: Throwable?) {
        delegate.debug("", error)
    }

    override fun debug(message: String?) {
        delegate.debug(message)
    }

    override fun debug(message: String?, throwable: Throwable?) {
        delegate.debug(message, throwable)
    }

    override fun isInfoEnabled(): Boolean {
        return delegate.isInfoEnabled
    }

    override fun info(message: String?) {
        delegate.info(message)
    }

    override fun info(message: String?, throwable: Throwable?) {
        delegate.info(message, throwable)
    }

    override fun info(content: CharSequence?) {
        info(content.toString())
    }

    override fun info(content: CharSequence?, error: Throwable?) {
        info(content.toString(), error)
    }

    override fun info(error: Throwable?) {
        info("", error)
    }

    override fun isWarnEnabled(): Boolean {
        return delegate.isWarnEnabled
    }

    override fun warn(message: String?) {
        delegate.warn(message)
    }

    override fun warn(message: String?, throwable: Throwable?) {
        delegate.warn(message, throwable)
    }

    override fun warn(content: CharSequence?) {
        warn(content.toString())
    }

    override fun warn(content: CharSequence?, error: Throwable?) {
        warn(content.toString(), error)
    }

    override fun warn(error: Throwable?) {
        warn("", error)
    }

    override fun isErrorEnabled(): Boolean {
        return delegate.isErrorEnabled
    }

    override fun error(message: String?) {
        delegate.error(message)
    }

    override fun error(message: String?, throwable: Throwable?) {
        delegate.error(message, throwable)
    }

    override fun error(content: CharSequence?) {
        error(content.toString())
    }

    override fun error(content: CharSequence?, error: Throwable?) {
        error(content.toString(), error)
    }

    override fun error(error: Throwable?) {
        error("", error)
    }

    override fun isFatalErrorEnabled(): Boolean {
        return delegate.isErrorEnabled
    }

    override fun fatalError(message: String?) {
        delegate.error(message)
    }

    override fun fatalError(message: String?, throwable: Throwable?) {
        delegate.error(message, throwable)
    }

}
