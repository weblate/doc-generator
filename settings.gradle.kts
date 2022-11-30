pluginManagement {
    plugins {
        val kotlinVersion = "1.7.21"
        kotlin("jvm") version kotlinVersion

        id("io.gitlab.arturbosch.detekt") version "1.22.0"

        id("com.github.jakemarsden.git-hooks") version "0.0.2"

        id("org.cadixdev.licenser") version "0.6.1"
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "doc-generator"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}
