plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.shadow)
    implementation(libs.indra.common)
    implementation(libs.indra.licenseHeader)
    implementation(libs.indra.crossdoc)

    // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
