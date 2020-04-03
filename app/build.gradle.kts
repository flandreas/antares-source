val mockkVersion: String by extra
val slf4jVersion: String by extra
val commonsIoVersion: String by extra
val commonsLang3Version: String by extra
val l2fprodVersion: String by extra

kotlin {

	sourceSets {

		val commonMain by getting {
			dependencies {
				implementation(project(":base"))
				implementation(project(":io"))
				implementation(project(":animation"))
				implementation(project(":draw"))
				implementation(project(":edit"))
			}
		}

		val jvmMain by getting {
			dependencies {
				implementation("commons-cli:commons-cli:1.3.1")
			}
		}
	}
}