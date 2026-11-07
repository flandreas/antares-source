kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":draw"))
				implementation(project(":base"))
				implementation(project(":animation"))
				implementation(project(":base-ui"))
			}
		}
	}
}
