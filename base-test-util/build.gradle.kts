kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":base"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":base"))
            }
        }
    }
}