kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":base"))
				implementation(project(":animation"))
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(project(":draw-test-util"))
			}
		}
	}
}
