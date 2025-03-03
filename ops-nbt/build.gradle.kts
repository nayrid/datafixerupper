plugins {
    id("serialize.base-conventions")
}

dependencies {
    api(project(":serialize-api"))
    api(libs.nbt)
}
