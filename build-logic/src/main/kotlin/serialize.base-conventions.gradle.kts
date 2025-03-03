import com.diffplug.gradle.spotless.FormatExtension

plugins {
    `java-library`
    idea
    id("net.kyori.indra")
    id("net.kyori.indra.checkstyle")
    id("net.kyori.indra.licenser.spotless")
    `maven-publish`
}

indra {
    mitLicense()

    javaVersions {
        target(21)
    }
}

indraSpotlessLicenser {
    licenseHeaderFile(rootProject.file("license.txt"))
}

spotless {
    fun FormatExtension.applyCommon() {
        trimTrailingWhitespace()
        endWithNewline()
        indentWithSpaces(4)
    }
    java {
        importOrderFile(rootProject.file(".spotless/nayrid.importorder"))
        applyCommon()
    }
    kotlinGradle {
        applyCommon()
    }
}

dependencies {
    checkstyle(libs.stylecheck)

    compileOnlyApi(libs.jspecify)
    compileOnlyApi(libs.jetbrains.annotations)

    testImplementation(libs.junit.jupiterApi)
    testRuntimeOnly(libs.junit.jupiterEngine)

    implementation(libs.slf4j.api)

    testImplementation(libs.slf4j.simple)
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

val isSnapshot = (rootProject.version as String).contains(("SNAPSHOT"))

tasks {
    javadoc {
        (options as StandardJavadocDocletOptions).apply {
            isFailOnError = false
            links(
                "https://jspecify.dev/docs/api/",
                "https://javadoc.io/doc/org.jetbrains/annotations/26.0.2/",
                "https://javadoc.io/doc/com.google.code.findbugs/jsr305/3.0.2/",
                "https://www.javadoc.io/doc/com.google.code.gson/gson/2.8.0/",
                "https://www.slf4j.org/apidocs/",
                "https://guava.dev/releases/21.0/api/docs/",
                "https://commons.apache.org/proper/commons-lang/javadocs/api-3.5/"
            )
            tags(
                "apiNote:a:API Note:",
                "implSpec:a:Implementation Requirements:",
                "implNote:a:Implementation Note:",
                "param",
                "return",
                "throws",
                "since",
                "version",
                "serialData",
                "see"
            )
        }
    }
    test {
        useJUnitPlatform()
    }
}

publishing {
    repositories {
        maven {
            name = "kokirigladeNayrid"
            url = uri("https://repo.kokirigla.de/nayrid")
            credentials {
                username = project.findProperty("kokirigladeUsername") as String?
                    ?: System.getenv("MAVEN_NAME")
                password = project.findProperty("kokirigladePassword") as String?
                    ?: System.getenv("MAVEN_SECRET")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group as String + "." + rootProject.name
            version = rootProject.version as String
            from(components["java"])
        }
    }
}
