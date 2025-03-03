plugins {
    id("serialize.base-conventions")
}

dependencies {
    api(project(":${rootProject.name}-api"))
    api(libs.gson)
}
