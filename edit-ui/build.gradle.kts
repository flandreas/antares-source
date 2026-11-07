kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":edit"))
				implementation(project(":base"))
				implementation(project(":animation"))
				implementation(project(":io"))
				implementation(project(":draw"))
				implementation(project(":draw-ui"))
				implementation(project(":base-ui"))
			}
		}

		val jvmTest by getting {
			dependencies {
				implementation(project(":base-test-util"))
				implementation(project(":draw-test-util"))
				implementation(project(":edit-test-util"))
			}
		}
	}
}
