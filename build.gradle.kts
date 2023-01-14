import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-library`
    `maven-publish`
    signing

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

val isJitPack get() = "true" == System.getenv("JITPACK")


val name = "doc-generator"
val mavenGroup = "org.hyacinthbots"
val mavenVersion: String
    get() = if (isJitPack) System.getenv("RELEASE_TAG") else {
        val tag = System.getenv("GITHUB_TAG_NAME")
        val branch = System.getenv("GITHUB_BRANCH_NAME")
        when {
            !tag.isNullOrBlank() -> tag
            !branch.isNullOrBlank() && branch.startsWith("refs/heads/") ->
                branch.substringAfter("refs/heads/").replace("/", "-") + "-SNAPSHOT"

            else -> "undefined"
        }
    }

val commitHash get() = System.getenv("GITHUB_SHA") ?: "unknown"

val shortCommitHash get() = System.getenv("SHORT_SHA") ?: "unknown"

val description = "Generate documentation for KordEx bots!"
val projectUrl = "https://github.com/HyacinthBots/doc-generator"

val isUndefined: Boolean get() = mavenVersion == "undefined"
val isSnapshot: Boolean get() = mavenVersion.endsWith("-SNAPSHOT")
val isRelease get() = !isSnapshot && !isUndefined

val releasesUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
val snapshotsUrl = "https://oss.sonatype.org/content/repositories/snapshots/"

publishing {
    publications {
        create<MavenPublication>(name) {
            groupId = mavenGroup
            artifactId = name
            version = mavenVersion

            pom {
                name.set(name)
                description.set(description)
                url.set(projectUrl)

                organization {
                    name.set("HyacinthBots")
                    url.set("https://github.com/HyacinthBots")
                }

                developers {
                    developer {
                        name.set("The HyacinthBots team")
                    }
                }

                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/HyacinthBots/doc-generator/issues")
                }

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://mit-license.org/")
                    }
                }

                scm {
                    connection.set("scm:git:ssh://github.com/HyacinthBots/doc-generator.git")
                    developerConnection.set("scm:git:ssh://git@github.com:HyacinthBots/doc-generator.git")
                }
            }

            if (!isJitPack) {
                repositories {
                    maven {
                        url = uri(if (isSnapshot) snapshotsUrl else releasesUrl)

                        credentials {
                            username = System.getenv("NEXUS_USER")
                            password = System.getenv("NEXUS_PASSWORD")
                        }
                    }
                }
            }
        }
    }
}

if (!isJitPack && isRelease) {
    signing {
        val signingKey = findProperty("signingKey")?.toString()
        val signingPassword = findProperty("signingPassword")?.toString()
        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(String(Base64.getDecoder().decode(signingKey)), signingPassword)
        }
        sign(publishing.publications[name])
    }
}
