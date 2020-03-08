import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.konan.file.File.Companion.javaHome

val version_project: String by extra
val mockkVersion: String by extra
val slf4jVersion: String by extra
val commonsIoVersion: String by extra
val commonsLang3Version: String by extra
val l2fprodVersion: String by extra

plugins {
	id("com.github.johnrengelman.shadow") version "5.1.0"
}

kotlin {

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

	val shadowCreate by creating(ShadowJar::class) {
		dependsOn(assemble)
		manifest {
			attributes["Main-Class"] = "ch.scorpion.antares.AntaresSwing"
		}
		archiveClassifier.set("all")
		from(kotlin.jvm().compilations.getByName("main").output)
		configurations =
			mutableListOf(kotlin.jvm().compilations.getByName("main").compileDependencyFiles as Configuration)
	}

	val obfuscate by creating(proguard.gradle.ProGuardTask::class) {
		dependsOn(shadowCreate)

		injars("$buildDir/libs/antares-${version_project}-all.jar")
		outjars("$buildDir/libs/antares-${version_project}-obfuscated.jar")

		libraryjars("$javaHome/lib/rt.jar")
		libraryjars("$javaHome/lib/jce.jar")
		libraryjars("$javaHome/lib/jfxrt.jar")

		libraryjars("/Users/andreas/Documents/scorpion2/jabbah/lib/l2fprod-common-all-7.3.jar")
		libraryjars("$projectDir/../lib/exml-7.0.0.jar")

		val gradleUserHome = project.gradle.gradleUserHomeDir

		libraryjars("$gradleUserHome/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib/1.3.61/4702105e97f7396ae41b113fdbdc180ec1eb1e36/kotlin-stdlib-1.3.61.jar")
		libraryjars("$gradleUserHome/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-reflect/1.3.61/2e07c9a84c9e118efb70eede7e579fd663932122/kotlin-reflect-1.3.61.jar")
		libraryjars("$gradleUserHome/caches/modules-2/files-2.1/log4j/log4j/1.2.17/5af35056b4d257e4b64b9e8069c0746e8b08629f/log4j-1.2.17.jar")
		libraryjars("$gradleUserHome/caches/modules-2/files-2.1/org.slf4j/slf4j-log4j12/1.7.21/7238b064d1aba20da2ac03217d700d91e02460fa/slf4j-log4j12-1.7.21.jar")
		libraryjars("$gradleUserHome/caches/modules-2/files-2.1/org.slf4j/slf4j-api/1.7.21/139535a69a4239db087de9bab0bee568bf8e0b70/slf4j-api-1.7.21.jar")

		keep("class kotlin.** { *; }")
		keep("class org.apache.** { *; }")
		keep("class com.l2fprod.** { *; }")

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

	val deploy by creating(Exec::class) {
		dependsOn(obfuscate)

		val version = file("shared/rsc/version.txt").readText().trim()
		val nativeType = when {
			System.getProperty("os.name").toLowerCase().contains("windows") -> "msi"
			System.getProperty("os.name").toLowerCase().contains("mac") -> "pkg"
			System.getProperty("os.name").toLowerCase().contains("linux") -> "rpm"
			else -> throw IllegalStateException("unknown system")
		}

		workingDir = projectDir
		commandLine(
			"javapackager",
			"-deploy",
			"-nosign",
			"-native", nativeType,
			"-outdir", "${buildDir}/distributions",
			"-outfile", project.name,
			"-name", "Antares",
			"-appclass", "ch.scorpion.antares.AntaresSwing",
			"-srcdir", "${buildDir}/libs",
			"-srcfiles", "antares-${version_project}-obfuscated.jar",
			"-BappVersion=${version}",
			"-Bicon=jvm/rsc/antares.icns"
		)
	}
}

