import dev.mokkery.gradle.ApplicationRule

mokkery {
    rule.set(ApplicationRule.All)
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":execution-test-util"))
                implementation(project(":draw"))
                implementation(project(":draw-test-util"))
                implementation(project(":edit"))
                implementation(project(":edit-test-util"))
                implementation(project(":app"))
                implementation(project(":graph"))
            }
        }
    }
}