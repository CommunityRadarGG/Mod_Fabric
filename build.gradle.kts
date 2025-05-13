/*
 * Copyright 2024 - present CommunityRadarGG <https://community-radar.de/>
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
import org.cadixdev.gradle.licenser.LicenseExtension

plugins {
    id("java")
    alias(libs.plugins.fabricLoom)
    alias(libs.plugins.cadixdevLicenser)
}

version = project.extra["mod_version"] as String
group = project.extra["maven_group"] as String

base {
    archivesName.set(project.extra["archives_base_name"] as String)
}

repositories {
    mavenCentral()
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft(libs.minecraft)
    mappings(libs.fabricYarn)
    modImplementation(libs.fabricLoader)

    // Make a set of all api modules we wish to use
    setOf(
        "fabric-api-base",
        "fabric-command-api-v2"
    ).forEach {
        // Add each module as a dependency
        modImplementation(fabricApi.module(it, libs.versions.fabricApi.get()))
    }

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    // withSourcesJar()
    // withJavadocJar()
}

tasks {
    withType<ProcessResources> {
        // https://github.com/gradle/gradle/issues/861
        outputs.upToDateWhen { false }

        filteringCharset = Charsets.UTF_8.name()
        filesMatching("fabric.mod.json") {
            expand(
                "version" to project.version,
                "loader_version" to libs.versions.fabricLoader.get(),
                "minecraft_version" to libs.versions.minecraft.get(),
                "website" to project.extra["website"],
                "source" to project.extra["source"],
                "discord" to project.extra["discord"]
            )
        }
    }

    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(21)
    }

    withType<Javadoc> {
        with(options as StandardJavadocDocletOptions) {
            encoding(Charsets.UTF_8.name())
            addStringOption("Xdoclint:reference,syntax,html,missing", "-quiet")
            keyWords()
            linkSource()
            use()
        }

        jar {
            from("LICENSE") {
                rename { "${it}_${project.base.archivesName.get()}" }
            }
        }
    }
}


configure<LicenseExtension> {
    newLine(false)
    header(rootProject.file("HEADER"))

    properties {
        set("year", "2024 - present")
        set("name", "CommunityRadarGG <https://community-radar.de/>")
    }

    tasks {
        create("gradle") {
            @Suppress("UnstableAPIUsage") // needed at that location
            files.from("build.gradle.kts", "settings.gradle.kts", "gradle.properties")
        }
    }
}
