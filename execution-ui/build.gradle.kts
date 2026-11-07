kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":execution"))
				implementation(project(":base"))
				implementation(project(":draw"))
				implementation(project(":animation"))
				implementation(project(":draw-ui"))
				implementation(project(":base-ui"))
			}
		}
	}
}
