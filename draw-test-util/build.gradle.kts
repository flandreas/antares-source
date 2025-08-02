import dev.mokkery.gradle.ApplicationRule

mokkery {
    rule.set(ApplicationRule.All)
}

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