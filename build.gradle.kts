import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `maven-publish`
    `java-gradle-plugin`

    kotlin("jvm")

    id("io.gitlab.arturbosch.detekt")
    id("com.github.jakemarsden.git-hooks")
    id("org.cadixdev.licenser")

    id("org.jetbrains.kotlinx.binary-compatibility-validator") version "0.12.1"
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

    api(libs.jetbrains.annotations)

    testImplementation(kotlin("test"))
}

gitHooks {
    setHooks(
        mapOf("pre-commit" to "clean apiCheck updateLicense detekt")
    )
}

gradlePlugin {
    plugins {
        create("doc-generator") {
            id = "org.hyacinthbots.gradle.docgenerator"
            implementationClass = "org.hyacinthbots.gradle.docgenerator.GeneratorGradlePlugin"
        }
    }
}

kotlin {
    explicitApi()
    jvmToolchain(javaVersion)
}

java {
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)

    withSourcesJar()
}

val sourceJar = task("sourceJar", Jar::class) {
    dependsOn(tasks["classes"])
    archiveClassifier.set("source")
    from(sourceSets.main.get().allSource)
}

val javadoc = task("javadocJar", Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
    from(tasks.javadoc)
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = javaVersion.toString()
            languageVersion = "1.7"
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
        gradleVersion = "7.6"
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
            artifact(javadoc)
            artifact(sourceJar)
        }
    }
}
