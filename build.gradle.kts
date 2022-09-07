import org.asciidoctor.gradle.AsciidoctorTask
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.gradle.internal.os.OperatingSystem

buildscript {
	dependencies {
		classpath("com.guardsquare:proguard-gradle:7.2.1")
	}
}

plugins {
	kotlin("multiplatform") version "1.5.30" apply false
	kotlin("plugin.serialization") version "1.5.30" apply false
	id("org.asciidoctor.convert") version "1.5.9.2"
	id("maven-publish")
}

val version_project: String by project
val group_project = rootProject.name

allprojects {

	repositories {
		maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
		maven("https://jitpack.io")
		mavenCentral()
		jcenter()
		flatDir {
			dirs("../lib")
		}

		// Publish Jabbah core libraries to Bytesave, so that Akrab can fetch from Bytesave
		maven {
			name = "bytesafe"
			url = uri("https://antares.bytesafe.dev/maven/antares/")
			credentials {
				username = "bytesafe"
				password = "01G4ADXPRW5PRW5H8SR9ZRGRFY"
			}
		}
	}

	group = group_project
	version = version_project

	buildDir = File(rootProject.projectDir, "build/${project.name}")
}

val kotlinWrappersVersion: String by extra
val ktorVersion: String by extra
val kotlinCoroutinesVersion: String by extra
val mockkVersion: String by extra
val slf4jVersion: String by extra
val commonsIoVersion: String by extra
val commonsLang3Version: String by extra
val commonsBeansVersion: String by extra
val commonsCodecVersion: String by extra
val l2fprodVersion: String by extra

subprojects {

	val projectName = this.name

	// jsBrowserTest doesn't work in JS targets due to open issues with mockk-js
	// See https://github.com/mockk/mockk/issues/100
	tasks.whenTaskAdded {
		if (this.name.contains("jsBrowserTest")) {
			this.enabled = false
		}
	}

	apply(plugin = "org.jetbrains.kotlin.multiplatform")
	apply(plugin = "kotlinx-serialization")
	apply(plugin = "maven-publish")

	configure<KotlinMultiplatformExtension> {
		jvm {
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

		if (OperatingSystem.current().isMacOsX) {
			js {
				browser()
			}
		}

		sourceSets {
			val commonMain by getting {
				kotlin.srcDir("shared/src/main")
				resources.srcDir("shared/rsc")
				dependencies {
					implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
					implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
				}
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
					implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")
					implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$kotlinCoroutinesVersion")
					implementation("io.ktor:ktor-client-core:$ktorVersion")
					implementation("io.ktor:ktor-client-cio:$ktorVersion")
					implementation("io.ktor:ktor-client-serialization:$ktorVersion")
					implementation("io.ktor:ktor-client-java:$ktorVersion")
					implementation("org.slf4j:slf4j-api:$slf4jVersion")
					implementation("org.slf4j:slf4j-log4j12:$slf4jVersion")
					implementation("commons-io:commons-io:$commonsIoVersion")
					implementation("commons-beanutils:commons-beanutils:$commonsBeansVersion")
					implementation("org.apache.commons:commons-lang3:$commonsLang3Version")
					implementation("commons-codec:commons-codec:$commonsCodecVersion")
					implementation("l2fprod:l2fprod-common-all:$l2fprodVersion")
					implementation("exml:exml:7.0")
					implementation("com.formdev:flatlaf:2.0.1")
					implementation("org.drjekyll:fontchooser:2.4")
				}

				// Workaround for bug in Gradle > 7.0 complaining about duplicate translation resources in generated JAR
				// (https://github.com/gradle/gradle/issues/17236)
				tasks {
					val jvmJar by getting(Jar::class) {
						duplicatesStrategy = DuplicatesStrategy.INCLUDE
					}
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

			if (OperatingSystem.current().isMacOsX) {
				val jsMain by getting {
					kotlin.srcDir("js/src/kotlin/main")
					resources.srcDir("js/rsc")

					dependencies {
						implementation("org.jetbrains.kotlin-wrappers:kotlin-wrappers-bom:${kotlinWrappersVersion}")
						implementation(npm("react-hot-loader", "^4.12.20"))
						implementation("org.jetbrains.kotlin-wrappers:kotlin-styled")
						implementation("com.ccfraser.muirwik:muirwik-components:0.9.0")
						implementation(npm("react-resize-detector", "~6.7.0"))
						implementation(npm("react-split-pane", "~0.1.92"))
						implementation(npm("@auth0/auth0-react", "~1.8.0"))
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

	publishing {
		publications {
			create<MavenPublication>("maven") {
				groupId = group_project
				artifactId = projectName
				version = version_project
			}
		}
		repositories {
			maven {
				name = "bytesafe"
				url = uri("https://antares.bytesafe.dev/maven/antares/")
				credentials {
					username = "bytesafe"
					password = "01G4ADXPRW5PRW5H8SR9ZRGRFY"
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

if (OperatingSystem.current().isMacOsX) {
	afterEvaluate {
		rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
			versions.webpackDevServer.version = "4.0.0"
		}
	}
}
