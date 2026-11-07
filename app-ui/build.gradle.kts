kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":app"))
				implementation(project(":base"))
				implementation(project(":io"))
				implementation(project(":animation"))
				implementation(project(":draw"))
				implementation(project(":edit"))
				implementation(project(":edit-ui"))
				implementation(project(":draw-ui"))
				implementation(project(":base-ui"))
			}
		}
	}
}
