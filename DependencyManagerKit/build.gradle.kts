plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "1.8.22"
}

kotlin {    
    macosArm64("native") {
        binaries {
            framework {
                baseName = "DependencyManagerKit"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
                implementation("io.ktor:ktor-client-cio:2.2.3")
            }
        }

        val nativeMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:2.2.3")
            }
        }

        all {
            languageSettings.languageVersion = "1.8"
            languageSettings.apiVersion = "1.8"
        }
    }
}