plugins {
    id("serialize.base-conventions")
}

dependencies {
    api(libs.guava)
    api(libs.fastutil)
}

tasks {
    javadoc {
        options {
            overview = "src/main/resources/overview.html"
        }
    }
}
