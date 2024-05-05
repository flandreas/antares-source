import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.konan.file.File.Companion.javaHome
import org.gradle.internal.os.OperatingSystem

val version_project: String by extra
val mockkVersion: String by extra
val slf4jVersion: String by extra
val commonsIoVersion: String by extra
val commonsLang3Version: String by extra
val l2fprodVersion: String by extra
val macOS_jpackage_home: String by extra
val win_jpackage_home: String by extra

// Values expected to be in user's home gradle.properties (NOT in project's gradle.properties under SCS!)
// Used for notarizing macOS package. The user name is Andreas' Apple ID for Antares, the password
// is an "Application Specific Password" defined in the Apple Account.
val appleNotarizationUser: String by extra
val appleNotarizationTeamId: String by extra
val appleNotarizationPassword: String by extra

repositories {
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
}

plugins {
	id("com.github.johnrengelman.shadow") version "5.1.0"
}

kotlin {

	if (OperatingSystem.current().isMacOsX) {
		js {
			browser {
				binaries.executable()
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
			}
		}
	}
}

tasks {

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
			attributes["SplashScreen-Image"] = "img/splash-light.png"
		}
		archiveClassifier.set("all")
		from(kotlin.jvm().compilations.getByName("main").output)
		configurations =
			mutableListOf(kotlin.jvm().compilations.getByName("main").runtimeDependencyFiles as Configuration)
		duplicatesStrategy = DuplicatesStrategy.INCLUDE
	}

	val copySplash by register<Copy>("copySplash") {
		from("$projectDir/jvm/rsc/img")
		include("splash*.png")
		into(file("$buildDir/package"))
	}

	val obfuscate by creating(proguard.gradle.ProGuardTask::class) {
		dependsOn(shadowCreate)

		configuration("proguard-rules.pro")

		injars("$buildDir/libs/antares-${version_project}-all.jar")
		outjars("$buildDir/package/antares-${version_project}.jar")

		libraryjars("$javaHome/jmods")

		keepkotlinmetadata()

		libraryjars(configurations.findByName("runtimeClasspath")?.files)

		keep("class javax.** { *; }")
		keep("class kotlin.** { *; }")
		keep("class org.apache.** { *; }")
		keep("class org.slf4j.** { *; }")
		keep("class com.l2fprod.** { *; }")
		keep("class com.formdev.** { *; }")
		keep("class io.ktor.** { *; }")
		keep("class kotlinx.coroutines.** { *; }")
		keep("class kotlinx.serialization.** { *; }")
		keep("class org.jdesktop.** { *; }")
		keep("class com.github.weisj.jsvg.** { *; }")

		// Reflection in OsThemeDetector
		keep("class com.sun.** { *; }")
		keep("class net.java.dev.** { *; }")
		keep("class sun.awt.** { *; }")
		keep("class de.jangassen.** { *; }")
		keep("class com.jthemedetecor.** { *; }")
		keep("class oshi.** { *; }")

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


		// Other reflective classes
		keep("class ch.scorpion.jabbah.base.swing.VerticalLabelUI { *; }")

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

	fun distributeMacSteps() {

		// Packaging and signing
		exec {
			workingDir = projectDir
			// Some of the following parameters are hard-wired in Info.plist
			commandLine(
				"${macOS_jpackage_home}/bin/jpackage",
				"--dest", "${buildDir}/distributions",
				"--input", "${buildDir}/package",
				"--name", "Antares",
				"--main-jar", "antares-${version_project}.jar",
				"--app-version", "$version_project",
				"--copyright", "Copyright (c) 2023 Andreas Fleischmann",
				"--vendor", "antarescircuit.io",
				"--icon", "jvm/rsc/antares.icns",
				"--java-options", "-splash:\$APPDIR/splash-light.png",
				"--java-options", "-Dapple.awt.application.name=Antares",
				"--java-options", "-Dapple.awt.application.appearance=system",
				"--resource-dir", "jvm/rsc/",
				"--mac-package-name", "Antares",
				"--mac-sign",
				"--mac-package-signing-prefix", "io.antarescircuit.Antares",
				"--mac-signing-key-user-name", "Andreas Fleischmann (WX94PVQXHK)",
			)
		}

		// Notarization (asynchronous call)
		exec {
			workingDir = projectDir
			commandLine(
				"xcrun", "notarytool",
				"submit",
				"--wait",
				"--apple-id", appleNotarizationUser,
				"--team-id", appleNotarizationTeamId,
				"--password", appleNotarizationPassword,
				"${buildDir}/distributions/Antares-$version_project.dmg"
			)
		}
	}

	/**
	 * Stapling of Apple notarization result not yet automized.
	 * Wait for notarization success confirmation mail from Apple and execute this task manually.
	 *
	 * Alternative notarization status check:
	 * xcrun notarytool info --apple-id appleNotarizationUser --team-id appleNotarizationTeamId --password appleNotarizationPassword [ID]
	 */
	val stapleMacNotarization by creating(Exec::class) {
		workingDir = projectDir
		commandLine(
			"xcrun", "stapler",
			"staple", "${buildDir}/distributions/Antares-$version_project.dmg"
		)
	}

	val distributeMac by creating {
		dependsOn(obfuscate)
		dependsOn(copySplash)

		doLast {
			distributeMacSteps()
		}
	}

	fun distributeWindowsSteps() {

		// Packaging
		exec {
			workingDir = projectDir
			commandLine(
				"${win_jpackage_home}\\bin\\jpackage",
				"--name", "Antares",
				"--input", "${buildDir}\\package",
				"--dest", "${buildDir}\\distributions",
				"--main-jar", "antares-${version_project}.jar",
				"--app-version", "$version",
				"--icon", "jvm\\rsc\\antares.ico",
				"--java-options", "-splash:\$APPDIR/splash-light.png",
				// Issue #522: Sorting problem in FileChooser under Windows
				"--java-options", "-Djava.util.Arrays.useLegacyMergeSort=true",
				"--type", "msi",
				"--resource-dir", "jvm/rsc/",
				"--win-shortcut"
			)
		}

		// Signing
		exec {
			workingDir = projectDir
			commandLine(
				"C:\\\"Program Files (x86)\"\\\"Windows Kits\"\\10\\bin\\10.0.22621.0\\x64\\signtool",
				"sign",
				"/fd", "SHA256",
				"/f", "C:\\Users\\Andreas\\Desktop\\AndreasFleischmann.pfx",
				"/t", "http://timestamp.digicert.com",
				"${buildDir}\\distributions\\Antares-$version_project.msi"
			)
		}
	}

	val distributeWindows by creating {
		dependsOn(obfuscate)
		dependsOn(copySplash)

		doLast {
			distributeWindowsSteps()
		}
	}
	
	val distributeLinux by creating(Exec::class) {
		dependsOn(obfuscate)
		dependsOn(copySplash)

		workingDir = projectDir
		
		commandLine(
			"jpackage",
			"--dest", "${buildDir}/distributions",
			"--input", "${buildDir}/package",
			"--name", "Antares",
			"--main-jar", "antares-${version_project}.jar",
			"--app-version", "$version_project",
			"--copyright", "Copyright (c) 2022 Andreas Fleischmann",
			"--vendor", "antarescircuit.io",
			"--icon", "jvm/rsc/antares-icon64.png",
			"--java-options", "-splash:\$APPDIR/splash-light.png",
			"--java-options", "-Dapple.awt.application.name=Antares",
			"--java-options", "-Dapple.awt.application.appearance=system",
			"--type", "rpm",
			"--resource-dir", "jvm/rsc/",
			"--linux-shortcut"
		)
	}
}
