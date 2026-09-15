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
    java
    alias(libs.plugins.fabricLoom)
    alias(libs.plugins.spotless)
}

val projectGroupId = providers.gradleProperty("group-id")
val projectVersion = providers.gradleProperty("version")
val archivesBaseName = providers.gradleProperty("archives-base-name")

group = projectGroupId.get()
version = projectVersion.get()

base {
    archivesName = archivesBaseName
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
        implementation(fabricApi.module(it, libs.versions.fabricApi.get()))
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

spotless {
    java {
        endWithNewline()
        licenseHeaderFile(rootProject.projectDir.resolve("HEADER"))
        importOrder("", "javax.|java.", "\\#")
        trimTrailingWhitespace()
        removeUnusedImports()
        forbidWildcardImports()
        forbidModuleImports()
        shortenFullyQualifiedTypes()
    }

    kotlinGradle {
        endWithNewline()
        licenseHeaderFile(rootProject.projectDir.resolve("HEADER"), "(import|plugins|buildscript|pluginManagement|base|root)")
        trimTrailingWhitespace()
    }

    kotlin {
        endWithNewline()
        licenseHeaderFile(rootProject.projectDir.resolve("HEADER"))
        trimTrailingWhitespace()
    }
}

tasks {
    val filterExpandProperties = mapOf(
        "version" to projectVersion.get(),
        "minecraft_version" to libs.versions.minecraft.get()
            .replace("-pre-", "-beta.")
            .replace("-rc-", "-rc."),
        "website" to providers.gradleProperty("website").get(),
        "source" to providers.gradleProperty("source").get(),
        "discord" to providers.gradleProperty("discord").get()
    )

    withType<ProcessResources>().configureEach {
        // https://github.com/gradle/gradle/issues/861
        inputs.properties(filterExpandProperties)

        filesMatching("fabric.mod.json") {
            expand(filterExpandProperties)
        }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = Charsets.UTF_8.name()
    }

    withType<Javadoc>().configureEach {
        with(options as StandardJavadocDocletOptions) {
            encoding(Charsets.UTF_8.name())
            keyWords()
            linkSource()
            use()
        }

        named<Jar>("jar") {
            from("LICENSE") {
                rename { "${it}_${archivesBaseName.get()}" }
            }
        }
    }
}
