import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.Project
import org.gradle.kotlin.dsl.the

// https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
val Project.libs: LibrariesForLibs
    get() = the()
