import com.diffplug.gradle.spotless.FormatExtension

plugins {
    `java-library`
    idea
    id("net.kyori.indra")
    id("net.kyori.indra.crossdoc")
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

val isSnapshot = (rootProject.version as String).contains(("SNAPSHOT"))

indraCrossdoc {
    baseUrl().set(providers.gradleProperty("javadocPublishRoot").get().replace("%REPO%", if(isSnapshot) "snapshots" else "releases"))
    nameBasedDocumentationUrlProvider {
        version = (project.version as String) + "/raw"
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

tasks {
    javadoc {
        (options as StandardJavadocDocletOptions).apply {
            isFailOnError = false
            links(
                // base
                "https://jspecify.dev/docs/api/",
                "https://javadoc.io/doc/org.jetbrains/annotations/26.0.2/",

                // api
                "https://guava.dev/releases/21.0/api/docs/",
                "https://www.slf4j.org/apidocs/",

                // ops-json
                "https://www.javadoc.io/doc/com.google.code.gson/gson/2.8.0/",

                // ops-nbt
                "https://jd.advntr.dev/nbt/4.19.0/"
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
            name = "nayridSnapshots"
            url = uri("https://repo.nayrid.com/snapshots")
            credentials {
                username = project.findProperty("nayridUsername") as String?
                    ?: System.getenv("MAVEN_NAME")
                password = project.findProperty("nayridPassword") as String?
                    ?: System.getenv("MAVEN_TOKEN")
            }
        }
        maven {
            name = "nayridReleases"
            url = uri("https://repo.nayrid.com/releases")
            credentials {
                username = project.findProperty("nayridUsername") as String?
                    ?: System.getenv("MAVEN_NAME")
                password = project.findProperty("nayridPassword") as String?
                    ?: System.getenv("MAVEN_TOKEN")
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
