@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalDistributionDsl::class)

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl

buildscript {
	repositories {
		mavenCentral()
	}
}

val kotlinVersion: String by extra

plugins {
	kotlin("multiplatform") version "2.3.20" apply false
	kotlin("plugin.serialization") version "1.9.23" apply false
	id("maven-publish")
	id("dev.mokkery") version "3.3.0" apply false
}

val version_project: String by project
val group_project = rootProject.name

allprojects {

	repositories {
		maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
		maven("https://jitpack.io")
		mavenCentral()
		flatDir {
			dirs("../lib")
		}
	}

	group = group_project
	version = version_project

	layout.buildDirectory = File(rootProject.projectDir, "build/${project.name}")
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

		targets.configureEach {
			compilations.configureEach {
				compileTaskProvider.get().compilerOptions {
					freeCompilerArgs.addAll(
						"-Xexpect-actual-classes",
						//"-Xes-long-as-bigint"
					)
				}
			}
		}

		jvm {

			compilerOptions {
				jvmTarget.set(JvmTarget.JVM_25)
				freeCompilerArgs.addAll(
					// https://youtrack.jetbrains.com/issue/KT-37435
					"-Xno-optimized-callable-references"
				)
			}
		}

		if (OperatingSystem.current().isMacOsX) {
			js(IR) {
				compilerOptions {
					freeCompilerArgs.add("-Xes-long-as-bigint")
				}

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
					optIn("kotlin.ExperimentalUnsignedTypes")
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

			commonTest {
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

					// Bean property sheet: Patched to support dark mode
					implementation(files("../lib/l2fprod-7.7.jar"))

					implementation("exml:exml:7.0")
					implementation("com.formdev:flatlaf:$flatLafVersion:no-natives")
					implementation("com.github.weisj:jsvg:$jsvgVersion")
					implementation("org.drjekyll:fontchooser:2.4")
					implementation("org.swinglabs.swingx:swingx-all:1.6.5-1")
					implementation("com.formdev:flatlaf-swingx:$flatLafVersion") {
						exclude("com.formdev", "flatlaf")
					}
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

					// Markdown HTML renderer and Swing viewer: Patched to support dark mode
					implementation(files("../lib/jmdviewer-1.1.jar"))

					api("org.commonmark:commonmark:0.17.1")
					api("org.commonmark:commonmark-ext-gfm-tables:0.17.1")
					api("commons-cli:commons-cli:1.4")

					// Markdown syntax highlighting
					implementation("com.fifesoft:rsyntaxtextarea:3.6.0")
				}

				// Workaround for bug in Gradle > 7.0 complaining about duplicate translation resources in generated JAR
				// (https://github.com/gradle/gradle/issues/17236)
				tasks {
					val jvmJar by getting(Jar::class) {
						duplicatesStrategy = DuplicatesStrategy.INCLUDE
					}
				}
			}

			jvmTest {
				kotlin.srcDir("jvm/src/test")
				dependencies {
					implementation(kotlin("test"))
					implementation(kotlin("test-junit"))
				}
			}

			if (OperatingSystem.current().isMacOsX) {
				jsMain {
					kotlin.srcDir("js/src/kotlin/main")
					resources.srcDir("js/rsc")
				}
				jsTest {
					kotlin.srcDir("js/src/kotlin/test")
					dependencies {
						implementation(kotlin("test-js"))
					}
				}
			}

			// Workaround for bug https://youtrack.jetbrains.com/issue/KT -24463:
			// Copy all resource files to the build directory used by IDEA run configuration
			tasks {
				val deployResources by registering(Copy::class) {
					from(listOf(commonMain.resources, jvmMain.resources)) {
						include("**/*.properties")
						include("**/libraries/**")
						include("**/img/*")
						include("**/version.txt")
					}
					into("${layout.buildDirectory}/classes/kotlin/jvm/main")
				}
				getByName("jvmMainClasses") {
					dependsOn(deployResources)
				}
			}
		}
	}

	publishing {

		// Bytesafe repository credentials: Taken from machine local gradle.properties
		val bytesaveUser: String? by extra
		val bytesavePassword: String? by extra

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

if (OperatingSystem.current().isMacOsX) {
	afterEvaluate {
		rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
			versions.webpackDevServer.version = "4.0.0"
		}
	}
}
