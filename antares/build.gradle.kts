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
	jcenter()
	maven {
		url = uri("https://dl.bintray.com/kotlin/kotlinx")
	}
	maven {
		url = uri("https://dl.bintray.com/kotlin/kotlin-js-wrappers")
	}
}

plugins {
	id("com.github.johnrengelman.shadow") version "5.1.0"
}

kotlin {

	js {
		browser {
			binaries.executable()
			webpackTask {
				cssSupport.enabled = true
			}
			runTask {
				cssSupport.enabled = true
			}
			testTask {
				useKarma {
					useChromeHeadless()
					webpackConfig.cssSupport.enabled = true
				}
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

		val jsMain by getting {
			dependencies {
				implementation(project(":base"))
				implementation(project(":animation"))
				implementation(project(":draw"))
				implementation(project(":edit"))
			}
		}
	}
}

tasks {

	val shadowCreate by creating(ShadowJar::class) {
		dependsOn(assemble)
		manifest {
			attributes["Main-Class"] = "ch.scorpion.antares.AntaresSwing"
			attributes["SplashScreen-Image"] = "img/splash.png"
		}
		archiveClassifier.set("all")
		from(kotlin.jvm().compilations.getByName("main").output)
		configurations =
			mutableListOf(kotlin.jvm().compilations.getByName("main").compileDependencyFiles as Configuration)
	}

	val copySplash by register<Copy>("copySplash") {
		from("$projectDir/shared/rsc/img")
		include("splash*.png")
		into(file("$buildDir/package"))
	}

	val obfuscate by creating(proguard.gradle.ProGuardTask::class) {
		dependsOn(shadowCreate)

		injars("$buildDir/libs/antares-${version_project}-all.jar")
		outjars("$buildDir/package/antares-${version_project}.jar")

		libraryjars("$javaHome/jmods")

		libraryjars("/Users/andreas/Documents/scorpion2/jabbah/lib/l2fprod-common-all-7.3.jar")
		libraryjars("$projectDir/../lib/exml-7.0.0.jar")

		val gradleUserHome = project.gradle.gradleUserHomeDir

		// TODO This is a hack! Make this independent of gradle cache.
		libraryjars("$gradleUserHome/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib/1.3.72/8032138f12c0180bc4e51fe139d4c52b46db6109/kotlin-stdlib-1.3.72.jar")
		libraryjars("$gradleUserHome/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-reflect/1.3.72/86613e1a669a701b0c660bfd2af4f82a7ae11fca/kotlin-reflect-1.3.72.jar")
		libraryjars("$gradleUserHome/caches/modules-2/files-2.1/log4j/log4j/1.2.17/5af35056b4d257e4b64b9e8069c0746e8b08629f/log4j-1.2.17.jar")
		libraryjars("$gradleUserHome/caches/modules-2/files-2.1/org.slf4j/slf4j-log4j12/1.7.21/7238b064d1aba20da2ac03217d700d91e02460fa/slf4j-log4j12-1.7.21.jar")
		libraryjars("$gradleUserHome/caches/modules-2/files-2.1/org.slf4j/slf4j-api/1.7.21/139535a69a4239db087de9bab0bee568bf8e0b70/slf4j-api-1.7.21.jar")
		libraryjars("$gradleUserHome/caches/modules-2/files-2.1/com.formdev/flatlaf/0.27/71392cc71b040b4fbc5b83927b99ae39ebab6acc/flatlaf-0.27.jar")

		keep("class kotlin.** { *; }")
		keep("class org.apache.** { *; }")
		keep("class com.l2fprod.** { *; }")
		keep("class com.formdev.** { *; }")

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
			"--input", "${buildDir}/package",
			"--dest", "${buildDir}/distributions",
			"--main-jar", "antares-${version_project}.jar",
			"--app-version", "$version",
			"--icon", "jvm/rsc/antares.icns",
			"--java-options", "-splash:\$APPDIR/splash.png",
			"--type", "pkg"
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
			"--java-options", "-splash:\$APPDIR/splash.png",
			"--type", "msi",
			"--win-shortcut"
		)
	}
}

