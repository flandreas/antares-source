import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

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
				implementation(project(":base"))
				implementation(project(":io"))
				implementation(project(":animation"))
				implementation(project(":draw"))
				implementation(project(":edit"))
				implementation(project(":app"))
				implementation(project(":execution"))
				implementation(project(":graph"))
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
		manifest {
			attributes["Main-Class"] = "ch.scorpion.antares.AntaresSwing"
		}
		archiveClassifier.set("all")
		from(kotlin.jvm().compilations.getByName("main").output)
		configurations =
			mutableListOf(kotlin.jvm().compilations.getByName("main").compileDependencyFiles as Configuration)
	}

	val build by existing {
		dependsOn(shadowCreate)
	}

	val run by creating(JavaExec::class) {
		dependsOn(shadowCreate)
		classpath = files("$buildDir/libs/antares-0.1-alpha-all.jar")
		main = "ch.scorpion.antares.AntaresSwing"
	}

	val deploy by creating(Exec::class) {
		dependsOn(assemble)

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
			"-srcdir", "build/libs",
			"-srcfiles", "antares-all.jar",
			"-BappVersion=${version}",
			"-Bicon=jvm/rsc/antares.icns"
		)
	}
}

