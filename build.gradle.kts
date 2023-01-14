import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-library`
    `maven-publish`

    alias(libs.plugins.kotlin)

    alias(libs.plugins.detekt)
    alias(libs.plugins.git.hooks)
    alias(libs.plugins.licenser)
    alias(libs.plugins.binary.compatibility.validator)
}

group = "org.hyacinthbots"
version = "0.1.0"
val javaVersion = 17

repositories {
    mavenCentral()

    maven {
        name = "Kotlin Discord"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }

    maven {
        name = "Sonatype Snapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }
}

dependencies {
    detektPlugins(libs.detekt)

    implementation(libs.kotlin.stdlib)
    implementation(libs.kordex)

    implementation(libs.logging)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(kotlin("test-junit5"))
    testRuntimeOnly(libs.junit.jupiter.engine)
}

gitHooks {
    setHooks(
        mapOf("pre-commit" to "clean apiCheck updateLicense detekt")
    )
}

kotlin {
    explicitApi()
}

java {
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)

    withSourcesJar()
}

if (JavaVersion.current() < JavaVersion.toVersion(javaVersion)) {
    kotlin.jvmToolchain(javaVersion)
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
            languageVersion = libs.plugins.kotlin.get().version.requiredVersion.substringBeforeLast(".")
            incremental = true
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=kotlin.RequiresOptIn"
            )
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.isDeprecation = true
        options.release.set(javaVersion)
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
    }
}

detekt {
    buildUponDefaultConfig = true
    config = files("$rootDir/detekt.yml")

    autoCorrect = true
}

license {
    setHeader(rootProject.file("HEADER"))
    include("**/*.kt", "**/*.java", "**/strings**.properties")
}

publishing {
    publications {
        create<MavenPublication>("publishToMavenLocal") {
            from(components.getByName("java"))
        }
    }
}
