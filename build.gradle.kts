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
	kotlin("multiplatform") version "1.4.0" apply false
	id("net.akehurst.kotlin.kt2ts") version("1.5.0") apply false
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
	apply(plugin = "org.jetbrains.kotlin.multiplatform")
	apply(plugin = "net.akehurst.kotlin.kt2ts")

	configure<KotlinMultiplatformExtension> {
		jvm() {
			// by default kotlin uses JavaVersion 1.6
			val main by compilations.getting {
				kotlinOptions {
					jvmTarget = JavaVersion.VERSION_1_8.toString()
					freeCompilerArgs = listOf(
						// https://youtrack.jetbrains.com/issue/KT-37435
						"-Xno-optimized-callable-references",
						"-Xinline-classes")
				}
			}
			val test by compilations.getting {
				kotlinOptions {
					jvmTarget = JavaVersion.VERSION_1_8.toString()
				}
			}
		}

		js(IR) {
			//browser()
			binaries.executable()
		}

		sourceSets {
			val commonMain by getting {
				kotlin.srcDir("shared/src/main")
				resources.srcDir("shared/rsc")
			}
			val commonTest by getting {
				kotlin.srcDir("shared/src/test")
				dependencies {
					implementation(kotlin("test"))
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
					implementation("com.formdev:flatlaf:0.27")
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
