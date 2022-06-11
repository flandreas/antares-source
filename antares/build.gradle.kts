import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.konan.file.File.Companion.javaHome

val version_project: String by extra
val mockkVersion: String by extra
val slf4jVersion: String by extra
val commonsIoVersion: String by extra
val commonsLang3Version: String by extra
val l2fprodVersion: String by extra
val macOS_jpackage_home: String by extra
val win_jpackage_home: String by extra

repositories {
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
}

plugins {
	id("com.github.johnrengelman.shadow") version "5.1.0"
}

kotlin {

	js(LEGACY) {
		browser {
			binaries.executable()
			commonWebpackConfig {
				cssSupport.enabled = true
			}
		}
	}

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
				api(project(":graph"))
			}
		}

		val jvmMain by getting {
			dependencies {
				implementation("commons-cli:commons-cli:1.3.1")
				implementation("com.github.Dansoftowner:jSystemThemeDetector:3.8")
			}
		}
	}
}

tasks {

	val deployTranslations by register<Copy>("deployTranslations") {
		from(
			file("$projectDir/../base/shared/rsc/jabbah-base_en.properties"),
			file("$projectDir/../draw/shared/rsc/jabbah-draw_en.properties"),
			file("$projectDir/../execution/shared/rsc/jabbah-execution_en.properties"),
			file("$projectDir/../edit/shared/rsc/jabbah-edit_en.properties"),
			file("$projectDir/../app/shared/rsc/jabbah-app_en.properties"),
			file("$projectDir/../graph/shared/rsc/jabbah-graph_en.properties")
		)
		into(file("$buildDir/processedResources/js/main"))
	}

	val combinedJar by creating(ShadowJar::class) {
		dependsOn(assemble)
		archiveClassifier.set("combined")
		from(kotlin.jvm().compilations.getByName("main").output)
		configurations =
			mutableListOf(kotlin.jvm().compilations.getByName("main").compileDependencyFiles as Configuration)
		dependencies {
			exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib:1.5.30"))
			exclude(dependency("org.jetbrains.kotlin:kotlin-reflect:1.5.30"))
		}
		duplicatesStrategy = DuplicatesStrategy.INCLUDE
	}


	val shadowCreate by creating(ShadowJar::class) {
		dependsOn(assemble)
		manifest {
			attributes["Main-Class"] = "ch.scorpion.antares.AntaresSwing"
			attributes["SplashScreen-Image"] = "img/splash-empty.png"
		}
		archiveClassifier.set("all")
		from(kotlin.jvm().compilations.getByName("main").output)
		configurations =
			mutableListOf(kotlin.jvm().compilations.getByName("main").runtimeDependencyFiles as Configuration)
		duplicatesStrategy = DuplicatesStrategy.INCLUDE
	}

	val copySplash by register<Copy>("copySplash") {
		from("$projectDir/shared/rsc/img")
		include("splash*.png")
		into(file("$buildDir/package"))
	}

	val obfuscate by creating(proguard.gradle.ProGuardTask::class) {
		dependsOn(shadowCreate)

		configuration("proguard-rules.pro")

		injars("$buildDir/libs/antares-${version_project}-all.jar")
		outjars("$buildDir/package/antares-${version_project}.jar")

		libraryjars("$javaHome/jmods")

		libraryjars("/Users/andreas/Documents/scorpion2/jabbah/lib/l2fprod-common-all-7.3.jar")
		libraryjars("$projectDir/../lib/exml-7.0.0.jar")

		keepkotlinmetadata()

		libraryjars(configurations.findByName("runtimeClasspath")?.files)

		keep("class kotlin.** { *; }")
		keep("class org.apache.** { *; }")
		keep("class com.l2fprod.** { *; }")
		keep("class com.formdev.** { *; }")
		keep("class io.ktor.** { *; }")
		keep("class kotlinx.coroutines.** { *; }")

		// Reflection in OsThemeDetector
		keep("class com.sun.** { *; }")
		keep("class net.java.dev.** { *; }")
		keep("class sun.awt.** { *; }")
		keep("class de.jangassen.** { *; }")
		keep("class com.jthemedetecor.** { *; }")

		// Logging using reflection
		keep("enum ch.scorpion.jabbah.base.LogLevel { *; }")

		// JavaBeans introspection used for PropertyPanels
		keep("interface ch.scorpion.jabbah.edit.Bean { *; }")
		keep("class * implements ch.scorpion.jabbah.edit.Bean { *; }")
		keep("class ch.scorpion.**.*BeanInfo { *; }")

		// Script DSL
		keep("class ch.scorpion.antares.script.dsl.* { *; }")

		// Main Application entry
		keep("class ch.scorpion.antares.AntaresSwing { void main(java.lang.String[]); }")

		// Classes with suspend methods and/or serialization
		keep("class kotlin.coroutines.Continuation { *; }")
		keep("class ch.scorpion.jabbah.app.rating.RatingService { *; }")
		keep("class ch.scorpion.jabbah.app.rating.RailwayRatingService { *; }")
		keepattributes("Signature")

		// Required by ResourceLibraryPersistenceService to copy standard library from JAR
		keep("class ch.scorpion.jabbah.graph.library.ResourceLibraryPersistenceService { *; }")
		keeppackagenames("ch.scorpion.jabbah.graph.library,ch.scorpion.jabbah.graph.library.ResourceLibraryPersistenceService")
		keepdirectories("libraries/**,ch/scorpion/jabbah/graph/library,ch/scorpion/jabbah/graph/library/ResourceLibraryPersistenceService")

		printmapping("$buildDir/libs/antares-${version_project}-proguard.map")
		renamesourcefileattribute("SourceFile")
		keepattributes("SourceFile,LineNumberTable")

		ignorewarnings()
	}


	val run by creating(JavaExec::class) {
		dependsOn(shadowCreate)
		classpath = files("$buildDir/libs/antares-${version_project}-all.jar")
		main = "ch.scorpion.antares.AntaresSwing"
	}

	val distributeMac by creating(Exec::class) {
		dependsOn(obfuscate)
		dependsOn(copySplash)

		val version = file("shared/rsc/version.txt").readText().trim()

		workingDir = projectDir

		commandLine(
			"${macOS_jpackage_home}/bin/jpackage",
			"--name", "Antares",
			"--mac-package-name", "Antares",
			"--input", "${buildDir}/package",
			"--dest", "${buildDir}/distributions",
			"--main-jar", "antares-${version_project}.jar",
			"--app-version", "$version",
			"--icon", "jvm/rsc/antares.icns",
			"--java-options", "-splash:\$APPDIR/splash-empty.png",
			"--java-options", "-Dapple.awt.application.name=Antares",
			"--java-options", "-Dapple.awt.application.appearance=system",
			"--type", "pkg",
			"--resource-dir", "jvm/rsc/"
		)
	}

	val distributeWindows by creating(Exec::class) {
		dependsOn(obfuscate)
		dependsOn(copySplash)

		val version = file("shared/rsc/version.txt").readText().trim()

		workingDir = projectDir

		commandLine(
			"${win_jpackage_home}\\bin\\jpackage",
			"--name", "Antares",
			"--input", "${buildDir}\\package",
			"--dest", "${buildDir}\\distributions",
			"--main-jar", "antares-${version_project}.jar",
			"--app-version", "$version",
			"--icon", "jvm\\rsc\\antares.ico",
			"--java-options", "-splash:\$APPDIR/splash-empty.png",
			"--type", "msi",
			"--resource-dir", "jvm/rsc/",
			"--win-shortcut"
		)
	}
}
