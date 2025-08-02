import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

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
				implementation(project(":animation"))
				implementation(project(":io"))
				implementation(project(":draw"))
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(project(":draw-test-util"))
				implementation(project(":edit-test-util"))
			}
		}
	}
}