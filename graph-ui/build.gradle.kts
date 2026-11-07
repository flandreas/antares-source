kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":graph"))
				implementation(project(":base"))
				implementation(project(":io"))
				implementation(project(":animation"))
				implementation(project(":draw"))
				implementation(project(":edit"))
				implementation(project(":app"))
				implementation(project(":execution"))
				implementation(project(":app-ui"))
				implementation(project(":execution-ui"))
				implementation(project(":edit-ui"))
				implementation(project(":draw-ui"))
				implementation(project(":base-ui"))
			}
		}

		val jvmTest by getting {
			dependencies {
				implementation(project(":graph-test-util"))
				implementation(project(":edit-test-util"))
				implementation(project(":app-test-util"))
			}
		}
	}
}
