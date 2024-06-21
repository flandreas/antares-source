import org.asciidoctor.gradle.AsciidoctorTask
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.gradle.internal.os.OperatingSystem

buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("com.guardsquare:proguard-gradle:7.4.0-beta02")
	}
}

val kotlinVersion: String by extra

plugins {
	kotlin("multiplatform") version "2.0.0" apply false
	kotlin("plugin.serialization") version "1.9.23" apply false
	id("org.asciidoctor.convert") version "1.5.9.2"
	id("maven-publish")
	id("dev.mokkery") version "2.0.0" apply false
}

val version_project: String by project
val group_project = rootProject.name

// Bytesafe repository: Taken from machine local gradle.properties
val bytesaveUser: String by extra
val bytesavePassword: String by extra

allprojects {

	repositories {
		maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
		maven("https://jitpack.io")
		mavenCentral()
		flatDir {
			dirs("../lib")
		}

		// Publish Jabbah core libraries to Bytesave, so that Akrab can fetch from Bytesave
		maven {
			name = "bytesafe"
			url = uri("https://antares.bytesafe.dev/maven/antares/")
			credentials {
				username = bytesaveUser
				password = bytesavePassword
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
val slf4jVersion: String by extra
val commonsIoVersion: String by extra
val commonsLang3Version: String by extra
val commonsBeansVersion: String by extra
val commonsCodecVersion: String by extra
val commonsMathVersion: String by extra
val l2fprodVersion: String by extra
val flatLafVersion: String by extra
val korteVersion: String by extra
val batikVersion: String by extra
val jsvgVersion: String by extra

subprojects {

	val projectName = this.name

	apply(plugin = "org.jetbrains.kotlin.multiplatform")
	apply(plugin = "kotlinx-serialization")
	apply(plugin = "maven-publish")
	apply(plugin = "dev.mokkery")

	configure<KotlinMultiplatformExtension> {
		withSourcesJar(publish = false)

		jvm {

			// by default kotlin uses JavaVersion 1.6
			val main by compilations.getting {
				kotlinOptions {
					jvmTarget = JavaVersion.VERSION_1_8.toString()
					freeCompilerArgs = listOf(
						// https://youtrack.jetbrains.com/issue/KT-37435
						"-Xno-optimized-callable-references",
						"-Xexpect-actual-classes",
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
			js(IR) {
				browser {
					commonWebpackConfig {
						cssSupport {
							enabled.set(true)
						}
					}
				}
				generateTypeScriptDefinitions()
				binaries.library()
			}
		}

		sourceSets {
			all {
				languageSettings.apply {
					optIn("kotlin.js.ExperimentalJsExport")
				}
			}
			val commonMain by getting {
				kotlin.srcDir("shared/src/main")
				resources.srcDir("shared/rsc")
				dependencies {
					implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0")
					implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
					implementation("io.ktor:ktor-client-core:$ktorVersion")
					implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
					implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
					implementation("com.soywiz.korlibs.korte:korte:$korteVersion")
				}
			}
			val commonTest by getting {
				kotlin.srcDir("shared/src/test")
				dependencies {
					implementation(kotlin("test-common"))
					implementation(kotlin("test-annotations-common"))
					implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
				}
			}
			val jvmMain by getting {
				kotlin.srcDir("jvm/src/main")
				resources.srcDir("jvm/rsc")
				dependencies {
					implementation(kotlin("reflect"))
					implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")
					implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$kotlinCoroutinesVersion")
					implementation("io.ktor:ktor-client-apache:$ktorVersion")
					implementation("org.slf4j:slf4j-api:$slf4jVersion")
					implementation("org.slf4j:slf4j-log4j12:$slf4jVersion")
					implementation("commons-io:commons-io:$commonsIoVersion")
					implementation("commons-beanutils:commons-beanutils:$commonsBeansVersion")
					implementation("org.apache.commons:commons-lang3:$commonsLang3Version")
					implementation("org.apache.commons:commons-math3:$commonsMathVersion")
					implementation("commons-codec:commons-codec:$commonsCodecVersion")
					implementation("l2fprod:l2fprod-common-all:$l2fprodVersion")
					implementation("exml:exml:7.0")
					implementation("com.formdev:flatlaf:$flatLafVersion")
					implementation("com.github.weisj:jsvg:$jsvgVersion")
					implementation("org.drjekyll:fontchooser:2.4")
					implementation("org.swinglabs.swingx:swingx-all:1.6.5-1")
					implementation("com.formdev:flatlaf-swingx:$flatLafVersion")
					implementation("org.apache.xmlgraphics:batik-anim:$batikVersion")
					implementation("org.apache.xmlgraphics:batik-awt-util:$batikVersion")
					implementation("org.apache.xmlgraphics:batik-bridge:$batikVersion")
					implementation("org.apache.xmlgraphics:batik-css:$batikVersion")
					implementation("org.apache.xmlgraphics:batik-dom:$batikVersion")
					implementation("org.apache.xmlgraphics:batik-ext:$batikVersion")
					implementation("org.apache.xmlgraphics:batik-gvt:$batikVersion")
					implementation("org.apache.xmlgraphics:batik-parser:$batikVersion")
					implementation("org.apache.xmlgraphics:batik-script:$batikVersion")
					implementation("org.apache.xmlgraphics:batik-svg-dom:$batikVersion")
					implementation("org.apache.xmlgraphics:batik-svggen:$batikVersion")
					implementation("org.apache.xmlgraphics:batik-transcoder:$batikVersion")
					implementation("org.apache.xmlgraphics:batik-util:$batikVersion")
					implementation("org.apache.xmlgraphics:batik-xml:$batikVersion")
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
				}
			}

			if (OperatingSystem.current().isMacOsX) {
				val jsMain by getting {
					kotlin.srcDir("js/src/kotlin/main")
					resources.srcDir("js/rsc")
				}
				val jsTest by getting {
					kotlin.srcDir("js/src/kotlin/test")
					dependencies {
						implementation(kotlin("test-js"))
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
					username = bytesaveUser
					password = bytesavePassword
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
