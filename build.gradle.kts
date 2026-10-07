import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("jvm") version "2.2.20"
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "io.github.iineineno03k"
version = providers.gradleProperty("releaseVersion").getOrElse("0.1.0-SNAPSHOT")

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}

kotlin {
    explicitApi()
    jvmToolchain(21)
    // The published POM depends on this stdlib, and a Kotlin 2.0 consumer cannot read a newer one.
    coreLibrariesVersion = "2.0.0"
    compilerOptions {
        // Consumers on older compilers and JVMs can still read the published artifact.
        jvmTarget = JvmTarget.JVM_17
        languageVersion = KotlinVersion.KOTLIN_2_0
        apiVersion = KotlinVersion.KOTLIN_2_0
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral()
    // Signing keys exist only in the release workflow; local builds publish unsigned to mavenLocal.
    if (providers.gradleProperty("signingInMemoryKey").isPresent) {
        signAllPublications()
    }

    coordinates(group.toString(), "aaa-kt", version.toString())

    pom {
        name = "aaa-kt"
        description = "Arrange-Act-Assert as a typed call chain for Kotlin tests."
        url = "https://github.com/iineineno03k/aaa-kt"
        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
            }
        }
        developers {
            developer {
                id = "iineineno03k"
                name = "iineineno03k"
                url = "https://github.com/iineineno03k"
            }
        }
        scm {
            url = "https://github.com/iineineno03k/aaa-kt"
            connection = "scm:git:https://github.com/iineineno03k/aaa-kt.git"
            developerConnection = "scm:git:ssh://git@github.com/iineineno03k/aaa-kt.git"
        }
    }
}
