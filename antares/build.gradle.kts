import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
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
	id("com.gradleup.shadow") version "9.3.0"
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

		commonTest.dependencies {
			implementation(project(":base-test-util"))
			implementation(project(":draw-test-util"))
			implementation(project(":edit-test-util"))
			implementation(project(":graph-test-util"))
		}

		val jvmMain by getting {
			dependencies {
				implementation("commons-cli:commons-cli:1.3.1")
				implementation("org.apache.commons:commons-csv:1.14.0")
			}
		}
	}
}

tasks {

	val shadowCreate by registering(ShadowJar::class) {
		dependsOn(jvmMainClasses)
		manifest {
			attributes["Main-Class"] = "io.antarescircuit.antares.AntaresSwing"
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
		into(file("${layout.buildDirectory}/libs"))
	}

	val run by registering(JavaExec::class) {
		dependsOn(shadowCreate)
		classpath = files("${layout.buildDirectory}/libs/antares-${version_project}-all.jar")
		mainClass.set("io.antarescircuit.antares.AntaresSwing")
	}

	val packageMac by registering(Exec::class) {
		dependsOn(shadowCreate)
		dependsOn(copySplash)

		// Some of the following parameters are hard-wired in Info.plist
		commandLine(
			"${macOS_jpackage_home}/bin/jpackage",
			"--name", "Antares",
			"--input", "${project.layout.buildDirectory.dir("libs").get().asFile}",
			"--dest", "${project.layout.buildDirectory.dir("distributions").get().asFile}",
			"--main-jar", "antares-${version_project}-all.jar",
			"--app-version", version_project,
			"--copyright", "Copyright (c) 2026 Andreas Fleischmann",
			"--vendor", "antarescircuit.io",
			"--icon", "jvm/rsc/antares.icns",
			"--java-options", "-splash:\$APPDIR/splash-light.png",
			"--java-options", "-Dapple.awt.application.name=Antares",
			"--java-options", "-Dapple.awt.application.appearance=system",
			"--resource-dir", "jvm/rsc/",
			"--mac-package-name", "Antares",
			"--mac-sign",
			"--mac-package-signing-prefix", "io.antarescircuit.Antares.",
			"--mac-signing-key-user-name", "Andreas Fleischmann ($appleNotarizationTeamId)",
		)
	}

	val signDmgMac by registering(Exec::class) {
		dependsOn(packageMac)
		commandLine(
			"codesign",
			"--force",
			"--sign",
			"Developer ID Application: Andreas Fleischmann ($appleNotarizationTeamId)",
			"${project.layout.buildDirectory.dir("distributions").get().asFile}/antares-${version_project}.dmg",
		)
	}

	val notarizeMac by registering(Exec::class) {
		dependsOn(signDmgMac)
		workingDir = projectDir
		commandLine(
			"xcrun", "notarytool",
			"submit",
			"--wait",
			"--apple-id", appleNotarizationUser,
			"--team-id", appleNotarizationTeamId,
			"--password", appleNotarizationPassword,
			"${project.layout.buildDirectory.dir("distributions").get().asFile}/Antares-$version_project.dmg"
		)
	}

	/**
	 * Stapling of Apple notarization result not yet automized.
	 * Wait for notarization success confirmation mail from Apple and execute this task manually.
	 *
	 * Alternative notarization status check:
	 * xcrun notarytool info --apple-id appleNotarizationUser --team-id appleNotarizationTeamId --password appleNotarizationPassword [ID]
	 */
	val stapleMacNotarization by registering(Exec::class) {
		workingDir = projectDir
		commandLine(
			"xcrun", "stapler",
			"staple", "${project.layout.buildDirectory.dir("distributions").get().asFile}/Antares-$version_project.dmg"
		)
	}

	val distributeMac by registering {
		dependsOn(notarizeMac)
	}

	val distributeWindows by registering(Exec::class) {
		dependsOn(shadowCreate)
		dependsOn(copySplash)

		workingDir = projectDir

		commandLine(
			"${win_jpackage_home}\\bin\\jpackage",
			"--name", "Antares",
			"--input", "${{layout.buildDirectory}}\\libs",
			"--dest", "${{layout.buildDirectory}}\\distributions",
			"--main-jar", "antares-${version_project}-all.jar",
			"--app-version", version_project,
			"--copyright", "Copyright (c) 2026 Andreas Fleischmann",
			"--icon", "jvm\\rsc\\antares.ico",
			"--java-options", "-splash:\$APPDIR/splash-light.png",
			// Issue #522: Sorting problem in FileChooser under Windows
			"--java-options", "-Djava.util.Arrays.useLegacyMergeSort=true",
			"--type", "msi",
			"--resource-dir", "jvm/rsc/",
			"--win-shortcut"
		)
	}
	
	val distributeLinux by registering(Exec::class) {
		dependsOn(shadowCreate)
		dependsOn(copySplash)

		workingDir = projectDir

		commandLine(
			"jpackage",
			"--dest", "${{layout.buildDirectory}}/distributions",
			"--input", "${{layout.buildDirectory}}/libs",
			"--name", "Antares",
			"--main-jar", "antares-${version_project}-all.jar",
			"--app-version", version_project,
			"--copyright", "Copyright (c) 2026 Andreas Fleischmann",
			"--vendor", "antarescircuit.io",
			"--icon", "jvm/rsc/antares-icon64.png",
			"--java-options", "-splash:\$APPDIR/splash-light.png",
			"--java-options", "-Dapple.awt.application.name=Antares",
			"--java-options", "-Dapple.awt.application.appearance=system",
			"--java-options", "-Dsun.java2d.xrender=false",
			"--java-options", "-Dsun.java2d.pmoffscreen=true",
			"--type", "rpm",
			"--resource-dir", "jvm/rsc/",
			"--linux-shortcut"
		)
	}
}
