import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

object Meta {
    const val description = "Generate documentation for KordEx bots!"
    const val githubRepo = "HyacinthBots/doc-generator"
    const val release = "https://s01.oss.sonatype.org/service/local/"
    const val snapshot = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
}

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
    alias(libs.plugins.nexus.publish)
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

    withJavadocJar()
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

signing {
    val signingKey = providers.environmentVariable("GPG_SIGNING_KEY")
    val signingPass = providers.environmentVariable("GPG_SIGNING_PASS")

    if (signingKey.isPresent && signingPass.isPresent) {
        useInMemoryPgpKeys(signingKey.get(), signingPass.get())
        val extension = extensions.getByName("publishing") as PublishingExtension
        sign(extension.publications)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set(project.name)
                description.set(Meta.description)
                url.set("https://github.com/${Meta.githubRepo}")

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
                    url.set("https://github.com/${Meta.githubRepo}/issues")
                }

                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://mit-license.org/")
                    }
                }

                scm {
                    url.set("https://github.com/${Meta.githubRepo}.git")
                    connection.set("scm:git:git://github.com/${Meta.githubRepo}.git")
                    developerConnection.set("scm:git:git://github.com/#${Meta.githubRepo}.git")
                }
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri(Meta.release))
            snapshotRepositoryUrl.set(uri(Meta.snapshot))

            val ossrhUsername = System.getenv("NEXUS_USER")
            val ossrhPassword = System.getenv("NEXUS_PASSWORD")

            if (!ossrhUsername.isNullOrEmpty() && !ossrhPassword.isNullOrEmpty()) {
                username.set(ossrhUsername)
                password.set(ossrhPassword)
            }
        }
    }
}
