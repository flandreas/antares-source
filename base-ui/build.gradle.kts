kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":base"))
			}
		}

		val jvmTest by getting {
			dependencies {
				implementation(project(":base-test-util"))
			}
		}
	}
}
