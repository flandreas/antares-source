import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("com.gradleup.shadow") version "9.3.0"
}

val version_project: String by project

// Installer tool paths and signing credentials come from the user's local Gradle properties.
val macJpackageHome = providers.gradleProperty("macOS_jpackage_home")
val winJpackageHome = providers.gradleProperty("win_jpackage_home")
val appleNotarizationUser = providers.gradleProperty("appleNotarizationUser")
val appleNotarizationTeamId = providers.gradleProperty("appleNotarizationTeamId")
val appleNotarizationPassword = providers.gradleProperty("appleNotarizationPassword")

kotlin {
	sourceSets {
		val commonMain by getting {
			dependencies {
				implementation(project(":antares"))
				implementation(project(":base"))
				implementation(project(":io"))
				implementation(project(":animation"))
				implementation(project(":draw"))
				implementation(project(":edit"))
				implementation(project(":app"))
				implementation(project(":execution"))
				implementation(project(":graph"))
				implementation(project(":graph-ui"))
				implementation(project(":app-ui"))
				implementation(project(":execution-ui"))
				implementation(project(":edit-ui"))
				implementation(project(":draw-ui"))
				implementation(project(":base-ui"))
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
	val shadowCreate by registering(ShadowJar::class) {
		group = "distribution"
		description = "Build the executable Antares desktop JAR"
		dependsOn(jvmMainClasses)
		archiveBaseName.set("antares")
		archiveClassifier.set("all")
		manifest {
			attributes["Main-Class"] = "io.antarescircuit.antares.AntaresSwing"
			attributes["SplashScreen-Image"] = "img/splash-light.png"
		}
		from(kotlin.jvm().compilations.getByName("main").output.allOutputs)
		configurations = mutableListOf(project.configurations.getByName("jvmRuntimeClasspath"))
		duplicatesStrategy = DuplicatesStrategy.INCLUDE
		exclude("test", "test/**")
	}

	val installerResources = layout.projectDirectory.dir("installer")
	val installerInput = layout.buildDirectory.dir("installer-input")
	val installerDestination = layout.buildDirectory.dir("distributions")
	val macInstaller = installerDestination.map { it.file("Antares-$version_project.dmg") }

	val prepareInstallerInput by registering(Sync::class) {
		group = "distribution"
		description = "Stage the desktop JAR and splash images for jpackage"
		from(shadowCreate.flatMap { it.archiveFile })
		from(layout.projectDirectory.dir("jvm/rsc/img")) {
			include("splash*.png")
		}
		into(installerInput)
	}

	val copySplash by registering {
		group = "distribution"
		description = "Compatibility entry point for staging the installer splash images"
		dependsOn(prepareInstallerInput)
	}

	val run by registering(JavaExec::class) {
		group = "application"
		dependsOn(shadowCreate)
		classpath = files(shadowCreate.flatMap { it.archiveFile })
		mainClass.set("io.antarescircuit.antares.AntaresSwing")
	}

	val packageMac by registering(Exec::class) {
		group = "distribution"
		dependsOn(prepareInstallerInput)
		workingDir = projectDir
		inputs.dir(installerResources)
		// Some of these parameters are also defined in the custom Info.plist.
		commandLine(
			"${macJpackageHome.get()}/bin/jpackage",
			"--name", "Antares",
			"--input", installerInput.get().asFile.absolutePath,
			"--dest", installerDestination.get().asFile.absolutePath,
			"--main-jar", "antares-$version_project-all.jar",
			"--app-version", version_project,
			"--copyright", "Copyright (c) 2026 Andreas Fleischmann",
			"--vendor", "antarescircuit.io",
			"--icon", installerResources.file("antares.icns").asFile.absolutePath,
			"--java-options", "-splash:\$APPDIR/splash-light.png",
			"--java-options", "-Dapple.awt.application.name=Antares",
			"--java-options", "-Dapple.awt.application.appearance=system",
			"--resource-dir", installerResources.asFile.absolutePath,
			"--mac-package-name", "Antares",
			"--mac-sign",
			"--mac-package-signing-prefix", "io.antarescircuit.Antares.",
			"--mac-signing-key-user-name", "Andreas Fleischmann (${appleNotarizationTeamId.get()})",
		)
	}

	val signDmgMac by registering(Exec::class) {
		group = "distribution"
		dependsOn(packageMac)
		commandLine(
			"codesign",
			"--force",
			"--sign", "Developer ID Application: Andreas Fleischmann (${appleNotarizationTeamId.get()})",
			macInstaller.get().asFile.absolutePath,
		)
	}

	val notarizeMac by registering(Exec::class) {
		group = "distribution"
		dependsOn(signDmgMac)
		workingDir = projectDir
		commandLine(
			"xcrun", "notarytool", "submit",
			"--wait",
			"--apple-id", appleNotarizationUser.get(),
			"--team-id", appleNotarizationTeamId.get(),
			"--password", appleNotarizationPassword.get(),
			macInstaller.get().asFile.absolutePath,
		)
	}

	// Manual follow-up after notarization succeeds; this task does not submit another installer.
	val stapleMacNotarization by registering(Exec::class) {
		group = "distribution"
		workingDir = projectDir
		commandLine("xcrun", "stapler", "staple", macInstaller.get().asFile.absolutePath)
	}

	val distributeMac by registering {
		group = "distribution"
		dependsOn(notarizeMac)
	}

	val distributeWindows by registering(Exec::class) {
		group = "distribution"
		dependsOn(prepareInstallerInput)
		workingDir = projectDir
		inputs.dir(installerResources)
		commandLine(
			"${winJpackageHome.get()}\\bin\\jpackage",
			"--name", "Antares",
			"--input", installerInput.get().asFile.absolutePath,
			"--dest", installerDestination.get().asFile.absolutePath,
			"--main-jar", "antares-$version_project-all.jar",
			"--app-version", version_project,
			"--copyright", "Copyright (c) 2026 Andreas Fleischmann",
			"--icon", installerResources.file("antares.ico").asFile.absolutePath,
			"--java-options", "-splash:\$APPDIR/splash-light.png",
			// Issue #522: Sorting problem in FileChooser under Windows.
			"--java-options", "-Djava.util.Arrays.useLegacyMergeSort=true",
			"--type", "msi",
			"--resource-dir", installerResources.asFile.absolutePath,
			"--win-shortcut",
		)
	}

	val distributeLinux by registering(Exec::class) {
		group = "distribution"
		dependsOn(prepareInstallerInput)
		workingDir = projectDir
		inputs.dir(installerResources)
		commandLine(
			"jpackage",
			"--input", installerInput.get().asFile.absolutePath,
			"--dest", installerDestination.get().asFile.absolutePath,
			"--name", "Antares",
			"--main-jar", "antares-$version_project-all.jar",
			"--app-version", version_project,
			"--copyright", "Copyright (c) 2026 Andreas Fleischmann",
			"--vendor", "antarescircuit.io",
			"--icon", installerResources.file("antares-icon64.png").asFile.absolutePath,
			"--java-options", "-splash:\$APPDIR/splash-light.png",
			"--java-options", "-Dapple.awt.application.name=Antares",
			"--java-options", "-Dapple.awt.application.appearance=system",
			"--java-options", "-Dsun.java2d.xrender=false",
			"--java-options", "-Dsun.java2d.pmoffscreen=true",
			"--type", "rpm",
			"--resource-dir", installerResources.asFile.absolutePath,
			"--linux-shortcut",
		)
	}
}
