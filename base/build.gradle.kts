kotlin {
    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(project(":base-test-util"))
            }
        }
    }
}