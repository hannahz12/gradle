/*
 * Copyright 2023 the original author or authors.
 *
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
 */

package org.gradle.internal.restricteddsl


import org.gradle.integtests.fixtures.AbstractIntegrationSpec

class RestrictedDslProjectSettingsIntegrationSpec extends AbstractIntegrationSpec {

    def "can interpret the settings file with the restricted DSL"() {
        given:
        file("settings.gradle.something") << """
            rootProject.name = "test-value"
            include(":a")
            include(":b")

            dependencyResolutionManagement {
                repositories {
                    mavenCentral()
                    google()
                }
            }
            pluginManagement {
                includeBuild("pluginIncluded")
                repositories {
                    mavenCentral()
                    google()
                }
            }
        """
        buildFile << "println('name = ' + rootProject.name)"
        file("a/build.gradle") << ""
        file("b/build.gradle") << ""
        file("pluginIncluded/settings.gradle.something") << "rootProject.name = \"pluginIncluded\""

        expect:
        succeeds(":help", ":a:help", ":b:help")
        outputContains("name = test-value")
    }

    def 'schema is written during settings interpretation'() {
        given:
        file("settings.gradle.something") << """
            rootProject.name = "test"
        """

        when:
        run(":help")

        then:
        def schemaFile = file(".gradle/restricted-schema/settings.something.schema")
        schemaFile.isFile() && schemaFile.text != ""
    }

}
