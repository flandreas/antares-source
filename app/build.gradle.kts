val mockkVersion: String by extra
val slf4jVersion: String by extra
val commonsIoVersion: String by extra
val commonsLang3Version: String by extra
val l2fprodVersion: String by extra

plugins {
	kotlin("multiplatform")
}

kotlin {

	jvm()
	js()

	sourceSets {

		val commonMain by getting {
			kotlin.srcDir("shared/src/main")
			resources.srcDir("shared/rsc")
			dependencies {
				implementation(project(":base"))
				implementation(project(":io"))
				implementation(project(":animation"))
				implementation(project(":draw"))
				implementation(project(":edit"))
				implementation(kotlin("stdlib-common"))
			}
		}

		val commonTest by getting {
			kotlin.srcDir("shared/src/test")
			dependencies {
				implementation(kotlin("test"))
				implementation(kotlin("reflect"))
				implementation(kotlin("test-common"))
				implementation(kotlin("test-annotations-common"))
				implementation("io.mockk:mockk-common:$mockkVersion")
			}
		}

		val jvmMain by getting {
			kotlin.srcDir("jvm/src/main")
			dependencies {
				implementation(kotlin("stdlib-jdk8"))
				implementation(kotlin("reflect"))
				implementation("org.slf4j:slf4j-api:$slf4jVersion")
				implementation("org.slf4j:slf4j-log4j12:$slf4jVersion")
				implementation("commons-io:commons-io:$commonsIoVersion")
				implementation("org.apache.commons:commons-lang3:$commonsLang3Version")
				implementation("l2fprod:l2fprod-common-all:$l2fprodVersion")
				implementation("mind:exml:7.0.0")
				implementation("commons-cli:commons-cli:1.3.1")
			}
		}

		val jvmTest by getting {
			kotlin.srcDir("jvm/src/test")
			dependencies {
				implementation(kotlin("test-junit"))
				implementation("io.mockk:mockk:$mockkVersion")
			}
		}

		val jsMain by getting {
			kotlin.srcDir("js/src/kotlin/main")
			dependencies {
				implementation(kotlin("stdlib-js"))
			}
		}

		val jsTest by getting {
			kotlin.srcDir("js/src/kotlin/test")
			dependencies {
				implementation(kotlin("test-js"))
				implementation("io.mockk:mockk-js:1.7.17")
			}
		}
	}
}