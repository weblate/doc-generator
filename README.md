## Doc-Generator
Doc-generator is a small library utility for the [KordEx](https://github.com/Kord-Extensions/kord-extensions) library 
that automatically generates documentation for commands within your KordEx bot.

This project is licensed under the [MIT License](https://mit-license.org/)

## Installation

### Latest version

#### Maven:

```xml
<!-- Adding the Jitpack repository -->
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
```xml
<!-- Adding the dependency. Replace TAG with the latest version -->
<dependency>
    <groupId>com.github.hyacinthbots</groupId>
    <artifactId>doc-generator</artifactId>
    <version>TAG</version>
</dependency>
```

#### Gradle (Groovy)

```groovy
// Adding the Jitpack repository
repositories {
    maven {
        name = 'Jitpack'
        url = 'https://jitpack.io'
    }
}

// Adding the dependency. Replace TAG with the latest version
dependencies {
    implementation('com.github.hyacinthbots:doc-generator:TAG')
}
```

#### Gradle (Kotlin)
```kotlin
// Adding the Jitpack repository
repositories {
    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")
    }
}

// Adding the dependency. Replace TAG with latest version
dependencies {
    implementation("com.github.hyacinthbots:doc-generator:TAG")
}
```

### Getting started
Once the dependency has been added, navigate to the [usage guide](/docs/usage-guide.md) for how to get started.
