rootProject.name = "ktor-di-overview"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":dagger")
include(":kotlin-inject")
include(":koin")
include(":kodein")
include(":ktor-di")
