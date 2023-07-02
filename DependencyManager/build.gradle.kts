plugins {
    kotlin("multiplatform")
    // application
}

kotlin {
    // jvm()
    // jvm("jvm") {
    //     compilations.main {
    //         tasks["compileKotlin"].kotlinOptions.jvmTarget = "1.8"
    //         tasks["compileKotlin"].kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
    //     }
    // }

    macosArm64("native") {
        binaries {
            executable {
                entryPoint = "dependency.manager.macos.main"
            }
        }
    }
    
    sourceSets {
        val nativeMain by getting

        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation(project(":DependencyManagerKit"))
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

// application {
//     mainClassName = "dependency.manager.MainKt"
// }

// tasks {
//     compileKotlin {
//         kotlinOptions.jvmTarget = "1.8"
//     }

//     compileTestKotlin {
//         kotlinOptions.jvmTarget = "1.8"
//     }
// }