kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":base"))
                implementation(project(":draw"))
            }
        }
    }
}