plugins {
    kotlin("multiplatform")
}

kotlin {
    // jvm()
    
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
            }
        }

        // val jvmMain by getting {
        //     dependencies {
        //         implementation(kotlin("stdlib-jdk8"))
        //     }
        // }
    }
}
