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

plugins {
    id("java")
    alias(libs.plugins.fabricLoom)
    alias(libs.plugins.spotless)
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
    minecraft(libs.minecraft)
    implementation(libs.fabricLoader)

    setOf(
        "fabric-api-base",
        "fabric-command-api-v2"
    ).forEach {
        // Add each module as a dependency
        implementation(fabricApi.module(it, libs.versions.fabricApi.get()))
    }

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    // withSourcesJar()
    // withJavadocJar()
}

tasks {
    var archivesName = project.base.archivesName.get()
    val filterExpandProps = mapOf(
        "version" to project.version,
        "loader_version" to libs.versions.fabricLoader.get(),
        "minecraft_version" to libs.versions.minecraft.get()
            .replace("-pre", "-beta.")
            .replace("-rc", "-rc."),
        "website" to project.extra.get("website") as String,
        "source" to project.extra.get("source") as String,
        "discord" to project.extra.get("discord") as String
    )

    withType<ProcessResources> {
        // https://github.com/gradle/gradle/issues/861
        outputs.upToDateWhen { false }

        filteringCharset = Charsets.UTF_8.name()
        filesMatching("fabric.mod.json") {
            expand(filterExpandProps)
        }
    }

    withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(25)
    }

    withType<Javadoc> {
        with(options as StandardJavadocDocletOptions) {
            encoding(Charsets.UTF_8.name())
            addStringOption("Xdoclint:reference,syntax,html,missing", "-quiet")
            keyWords()
            linkSource()
            use()
        }

        named<Jar>("jar") {
            from("LICENSE") {
                rename { "${it}_${archivesName}" }
            }
        }
    }
}

spotless {
    java {
        licenseHeaderFile(rootProject.file("HEADER"))
        endWithNewline()
        trimTrailingWhitespace()
        removeUnusedImports()
        forbidWildcardImports()
        forbidModuleImports()
    }

    kotlin {
        licenseHeaderFile(rootProject.file("HEADER"))
        endWithNewline()
        trimTrailingWhitespace()
    }
}
