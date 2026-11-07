@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalDistributionDsl::class)

import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.gradle.internal.os.OperatingSystem
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.jvm.tasks.Jar
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.attributes.java.TargetJvmVersion
import org.gradle.api.publish.maven.tasks.GenerateMavenPom
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
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

// Register JVM variant compatibility rules without Java source-set DSL accessors shadowing KMP's.
apply(plugin = "java-base")

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
					// Keep the serialization version previously supplied transitively by Ktor.
					implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
					implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
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
					implementation("org.slf4j:slf4j-api:$slf4jVersion")
					implementation("org.slf4j:slf4j-log4j12:$slf4jVersion")
					implementation("commons-io:commons-io:$commonsIoVersion")
					implementation("org.apache.commons:commons-lang3:$commonsLang3Version")
					implementation("org.apache.commons:commons-math3:$commonsMathVersion")
					implementation("commons-codec:commons-codec:$commonsCodecVersion")

					implementation("exml:exml:7.0")

					api("org.commonmark:commonmark:0.17.1")
					api("org.commonmark:commonmark-ext-gfm-tables:0.17.1")
					api("commons-cli:commons-cli:1.4")

					if (projectName.endsWith("-ui")) {
						// SVG image loading and rendering belong to the desktop UI.
						implementation("com.github.weisj:jsvg:$jsvgVersion")

						// Remote desktop services use Ktor; core consumers do not need an HTTP client.
						implementation("io.ktor:ktor-client-core:$ktorVersion")
						implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
						implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
						implementation("io.ktor:ktor-client-apache:$ktorVersion")

						// Batik used for SVG export
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

						implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:$kotlinCoroutinesVersion")
						implementation("commons-beanutils:commons-beanutils:$commonsBeansVersion")
						// Property sheet and Markdown viewer are patched for dark mode.
						api(files("../lib/l2fprod-7.7.jar"))
						implementation(files("../lib/jmdviewer-1.1.jar"))
						implementation("com.formdev:flatlaf:$flatLafVersion:no-natives")
						implementation("org.drjekyll:fontchooser:2.4")
						api("org.swinglabs.swingx:swingx-all:1.6.5-1")
						implementation("com.formdev:flatlaf-swingx:$flatLafVersion") {
							exclude("com.formdev", "flatlaf")
						}
						implementation("com.fifesoft:rsyntaxtextarea:3.6.0")
					}

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

val coreProjects = listOf(
	"base",
	"io",
	"animation",
	"draw",
	"edit",
	"execution",
	"app",
	"graph",
	"antares",
).map(::project)

val uiProjects = listOf(
	"base-ui",
	"draw-ui",
	"edit-ui",
	"execution-ui",
	"app-ui",
	"graph-ui",
	"antares-ui",
).map(::project)

fun Project.jvmJarFile(): Provider<RegularFile> =
	layout.buildDirectory.file("libs/${name}-jvm-$version_project.jar")

val coreJar by tasks.registering(Jar::class) {
	group = "distribution"
	description = "Aggregate Jabbah core JVM library"
	dependsOn(coreProjects.map { it.tasks.named("jvmJar") })
	archiveBaseName.set("jabbah-core")
	archiveVersion.set(version_project)
	destinationDirectory.set(layout.buildDirectory.dir("libs"))
	from(coreProjects.map { zipTree(it.jvmJarFile()) })
	from("LICENCE") {
		into("META-INF")
		rename { "LICENSE" }
	}
	// Antares file-based test circuits and their history are not production resources.
	exclude("test", "test/**")
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val libraryVersion = providers.gradleProperty("libraryVersion").get()
require(Regex("\\d+\\.\\d+\\.\\d+(-rc\\.[1-9]\\d*)?").matches(libraryVersion)) {
	"libraryVersion must be a release (2.5.0) or release candidate (2.5.0-rc.1)"
}

// Resolve all merged modules together so the POM records the same conflict-resolved JVM dependencies.
val coreModulePaths = coreProjects.map { it.path }
val corePublicationClasspath by configurations.creating {
	isCanBeConsumed = false
	isCanBeResolved = true
	attributes {
		attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
		attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
		attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
		attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
		attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
		attribute(KotlinPlatformType.attribute, KotlinPlatformType.jvm)
	}
}
coreProjects.forEach {
	dependencies.add(corePublicationClasspath.name, project(it.path))
}

val coreExternalDependencies = run {
	val allowedModulePaths = coreModulePaths
	corePublicationClasspath.incoming.artifacts.resolvedArtifacts.map { artifacts ->
		val uiLibraries = setOf(
			"flatlaf", "flatlaf-swingx", "swingx-all", "fontchooser", "rsyntaxtextarea",
			"kotlinx-coroutines-swing", "l2fprod", "jmdviewer", "jsvg",
		)
		artifacts.mapNotNull { artifact ->
			when (val component = artifact.id.componentIdentifier) {
				is ProjectComponentIdentifier -> {
					require(component.projectPath in allowedModulePaths) {
						"Core publication includes a non-core project: ${component.projectPath}"
					}
					null // These classes are already in coreJar.
				}
				is ModuleComponentIdentifier -> {
					require(component.module !in uiLibraries && !component.module.startsWith("batik-") &&
						!component.module.startsWith("ktor-client-")) {
						"UI dependency in core publication: $component"
					}
					val prefix = "${component.module}-${component.version}"
					val fileName = artifact.file.name
					require(fileName == "$prefix.jar" || (fileName.startsWith("$prefix-") && fileName.endsWith(".jar"))) {
						"Unsupported Maven artifact in core publication: $fileName"
					}
					val classifier = fileName.removePrefix(prefix).removeSuffix(".jar").removePrefix("-")
					"${component.group}:${component.module}:${component.version}:$classifier"
				}
				else -> error("Core file dependency needs publishable Maven coordinates: ${artifact.file.name}")
			}
		}.distinct().sorted()
	}
}

val coreSourcesJar by tasks.registering(Jar::class) {
	archiveBaseName.set("jabbah-core")
	archiveVersion.set(libraryVersion)
	archiveClassifier.set("sources")
	duplicatesStrategy = DuplicatesStrategy.FAIL
	coreProjects.forEach { module ->
		val sources = module.extensions.getByType<KotlinMultiplatformExtension>().sourceSets
		from(sources.getByName("commonMain").kotlin) { into("commonMain") }
		from(sources.getByName("jvmMain").kotlin) { into("jvmMain") }
	}
	from("LICENCE") {
		into("META-INF")
		rename { "LICENSE" }
	}
}

val coreJavadocJar by tasks.registering(Jar::class) {
	archiveBaseName.set("jabbah-core")
	archiveVersion.set(libraryVersion)
	archiveClassifier.set("javadoc")
	from("gradle/core-publication/index.html")
	from("doc/design-manual") { into("design-manual") }
	from("doc/jabbah-artifacts.md")
	from("doc/publishing.md")
	from("LICENCE") {
		into("META-INF")
		rename { "LICENSE" }
	}
}

val githubPackagesUsername = providers.gradleProperty("gpr.user")
	.orElse(providers.environmentVariable("GITHUB_ACTOR"))
val githubPackagesToken = providers.gradleProperty("gpr.key")
	.orElse(providers.environmentVariable("GITHUB_TOKEN"))

publishing {
	publications {
		create<MavenPublication>("core") {
			groupId = "io.antarescircuit"
			artifactId = "jabbah-core"
			version = libraryVersion
			artifact(coreJar)
			artifact(coreSourcesJar)
			artifact(coreJavadocJar)
			pom {
				val externalDependencies = coreExternalDependencies
				name.set("Jabbah Core")
				description.set("JVM core of the Jabbah framework and Antares circuit simulator, without desktop UI implementations.")
				url.set("https://github.com/flandreas/antares-source")
				licenses {
					license {
						name.set("The Apache License, Version 2.0")
						url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
						distribution.set("repo")
					}
				}
				developers {
					developer {
						id.set("flandreas")
						name.set("Andreas Fleischmann")
						url.set("https://github.com/flandreas")
					}
				}
				scm {
					url.set("https://github.com/flandreas/antares-source")
					connection.set("scm:git:https://github.com/flandreas/antares-source.git")
					developerConnection.set("scm:git:ssh://git@github.com/flandreas/antares-source.git")
				}
				withXml {
					val dependencies = asNode().appendNode("dependencies")
					externalDependencies.get().forEach { coordinates ->
						val (group, name, version, classifier) = coordinates.split(":", limit = 4)
						val dependency = dependencies.appendNode("dependency")
						dependency.appendNode("groupId", group)
						dependency.appendNode("artifactId", name)
						dependency.appendNode("version", version)
						if (classifier.isNotEmpty()) dependency.appendNode("classifier", classifier)
						dependency.appendNode("scope", "compile")
					}
				}
			}
		}
	}
	repositories {
		maven {
			name = "GitHubPackages"
			url = uri("https://maven.pkg.github.com/flandreas/antares-source")
			credentials {
				username = githubPackagesUsername.orNull
				password = githubPackagesToken.orNull
			}
		}
		maven {
			name = "coreValidation"
			url = uri(layout.buildDirectory.dir("publishing/core-validation"))
		}
	}
}

tasks.withType<GenerateMavenPom>().configureEach {
	inputs.property("coreExternalDependencies", coreExternalDependencies)
}

val checkGitHubPublishingConfiguration by tasks.registering {
	group = "publishing"
	description = "Check GitHub Packages credentials before upload"
	val username = githubPackagesUsername
	val token = githubPackagesToken
	doLast {
		check(!username.orNull.isNullOrBlank() && !token.orNull.isNullOrBlank()) {
			"Set gpr.user and gpr.key in ~/.gradle/gradle.properties, or GITHUB_ACTOR and GITHUB_TOKEN in the environment. Publishing requires write:packages access."
		}
	}
}
tasks.named("publishCorePublicationToGitHubPackagesRepository") {
	dependsOn(checkGitHubPublishingConfiguration)
}
tasks.register("publishCore") {
	group = "publishing"
	description = "Publish only jabbah-core to GitHub Packages"
	dependsOn("publishCorePublicationToGitHubPackagesRepository")
}
tasks.register("publishCoreForValidation") {
	group = "publishing"
	description = "Publish jabbah-core to a local file repository for consumer testing"
	dependsOn("publishCorePublicationToCoreValidationRepository")
}

val uiJar by tasks.registering(Jar::class) {
	group = "distribution"
	description = "Aggregate Jabbah UI JVM library"
	dependsOn(uiProjects.map { it.tasks.named("jvmJar") })
	archiveBaseName.set("jabbah-ui")
	archiveVersion.set(version_project)
	destinationDirectory.set(layout.buildDirectory.dir("libs"))
	from(uiProjects.map { zipTree(it.jvmJarFile()) })
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named("assemble") {
	dependsOn(coreJar, uiJar)
}
