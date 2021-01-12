import org.asciidoctor.gradle.AsciidoctorTask
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

buildscript {
	repositories {
		jcenter()
	}
	dependencies {
		classpath("net.sf.proguard:proguard-gradle:6.2.2")
	}
}

plugins {
	kotlin("multiplatform") version "1.4.21" apply false
	id("org.asciidoctor.convert") version "1.5.9.2"
}

allprojects {

	repositories {
		mavenCentral()
		jcenter()
		flatDir {
			dirs("../lib")
		}
	}

	val version_project: String by project
	val group_project = "${rootProject.name}"

	group = group_project
	version = version_project

	buildDir = File(rootProject.projectDir, "build/${project.name}")
}

val mockkVersion: String by extra
val slf4jVersion: String by extra
val commonsIoVersion: String by extra
val commonsLang3Version: String by extra
val commonsBeansVersion: String by extra
val l2fprodVersion: String by extra

subprojects {

	// jsBrowserTest doesn't work in JS targets due to open issues with mockk-js
	// See https://github.com/mockk/mockk/issues/100
	tasks.whenTaskAdded {
		if (this.name.contains("jsBrowserTest")) {
			this.enabled = false
		}
	}

	apply(plugin = "org.jetbrains.kotlin.multiplatform")

	configure<KotlinMultiplatformExtension> {
		jvm() {
			// by default kotlin uses JavaVersion 1.6
			val main by compilations.getting {
				kotlinOptions {
					jvmTarget = JavaVersion.VERSION_13.toString()
					freeCompilerArgs = listOf(
						// https://youtrack.jetbrains.com/issue/KT-37435
						"-Xno-optimized-callable-references",
						"-Xinline-classes")
				}
			}
			val test by compilations.getting {
				kotlinOptions {
					jvmTarget = JavaVersion.VERSION_13.toString()
				}
			}
		}

		js() {
			browser()
		}

		sourceSets {
			val commonMain by getting {
				kotlin.srcDir("shared/src/main")
				resources.srcDir("shared/rsc")
			}
			val commonTest by getting {
				kotlin.srcDir("shared/src/test")
				dependencies {
					implementation(kotlin("test-common"))
					implementation(kotlin("test-annotations-common"))
					implementation("io.mockk:mockk-common:$mockkVersion")
				}
			}
			val jvmMain by getting {
				kotlin.srcDir("jvm/src/main")
				dependencies {
					implementation(kotlin("reflect"))
					implementation("org.slf4j:slf4j-api:$slf4jVersion")
					implementation("org.slf4j:slf4j-log4j12:$slf4jVersion")
					implementation("commons-io:commons-io:$commonsIoVersion")
					implementation("commons-beanutils:commons-beanutils:$commonsBeansVersion")
					implementation("org.apache.commons:commons-lang3:$commonsLang3Version")
					implementation("l2fprod:l2fprod-common-all:$l2fprodVersion")
					implementation("mind:exml:7.0.0")
					implementation("com.formdev:flatlaf:0.43")
				}
			}
			val jvmTest by getting {
				kotlin.srcDir("jvm/src/test")
				dependencies {
					implementation(kotlin("test"))
					implementation(kotlin("test-junit"))
					implementation("io.mockk:mockk:$mockkVersion")
				}
			}
			val jsMain by getting {
				kotlin.srcDir("js/src/kotlin/main")
				resources.srcDir("js/rsc")

				dependencies {

					//React, React DOM + Wrappers (chapter 3)
					implementation("org.jetbrains:kotlin-react:17.0.1-pre.136-kotlin-1.4.10")
					implementation("org.jetbrains:kotlin-react-dom:17.0.1-pre.136-kotlin-1.4.10")
					implementation(npm("react", "17.0.1"))
					implementation(npm("react-dom", "17.0.1"))

					//Kotlin Styled (chapter 3)
					implementation("org.jetbrains:kotlin-styled:5.2.0-pre.136-kotlin-1.4.10")
					//implementation(npm("styled-components", "4.4.0"))
					implementation(npm("inline-style-prefixer", "~6.0.0"))

					implementation(npm("react-hot-loader", "^4.12.20"))
					implementation("com.ccfraser.muirwik:muirwik-components:0.6.3")
				}
			}
			val jsTest by getting {
				kotlin.srcDir("js/src/kotlin/test")
				dependencies {
					implementation(kotlin("test-js"))
					implementation("io.mockk:mockk-js:1.7.17")
				}
			}

			// Workaround for bug https://youtrack.jetbrains.com/issue/KT -24463:
			// Copy all resource files to the build directory used by IDEA run configuration
			tasks {
				val deployResources by creating(Copy::class) {
					from(listOf(commonMain.resources, jvmMain.resources)) {
						include("**/*.properties")
						include("**/libraries/**")
						include("**/img/*")
						include("**/version.txt")
					}
					into("${buildDir.absolutePath}/classes/kotlin/jvm/main")
				}
				getByName("jvmMainClasses") {
					dependsOn(deployResources)
				}
			}
		}
	}
}

tasks {
	register("copyImages", Copy::class) {
		from("doc/user-manual") {
		include("**/*.png")
		}
		into("build/doc/user-manual/html5")
	}

	"asciidoctor"(AsciidoctorTask::class) {
		dependsOn(getByName("copyImages"))
		sourceDir = file("doc/user-manual")
		outputDir = file("build/doc/user-manual")
	}
}
