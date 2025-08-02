val mockkVersion: String by extra
val slf4jVersion: String by extra
val commonsIoVersion: String by extra
val commonsLang3Version: String by extra
val l2fprodVersion: String by extra

kotlin {

	sourceSets {

		val commonMain by getting {
			dependencies {
				api(project(":base"))
				api(project(":io"))
				api(project(":animation"))
				api(project(":draw"))
				api(project(":edit"))
				api(project(":app"))
				api(project(":execution"))
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(project(":draw-test-util"))
				implementation(project(":edit-test-util"))
				implementation(project(":execution-test-util"))
				implementation(project(":app-test-util"))
				implementation(project(":graph-test-util"))
			}
		}

		val jvmMain by getting {
			dependencies {
				implementation("commons-cli:commons-cli:1.3.1")
			}
		}

		val jvmTest by getting {
			dependencies {
				implementation(project(":edit-test-util"))
			}
		}
	}
}