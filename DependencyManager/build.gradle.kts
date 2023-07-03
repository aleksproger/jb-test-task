plugins {
    kotlin("multiplatform")
}

kotlin {
    macosArm64("native") {
        binaries {
            executable {
                entryPoint = "dependency.manager.macos.main"
            }
        }
    }
    
    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation(project(":DependencyManagerKit"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
            }
        }
    }
}