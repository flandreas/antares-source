import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

abstract class GenerateJavaVersion : DefaultTask() {

	@get:Input
	abstract val javaVersion: Property<String>

	@get:OutputDirectory
	abstract val outputDirectory: DirectoryProperty

	@TaskAction
	fun generate() {
		val sourceFile = outputDirectory.file("io/antarescircuit/jabbah/app/JavaVersion.kt").get().asFile
		sourceFile.parentFile.mkdirs()
		sourceFile.writeText(
			"""
			|package io.antarescircuit.jabbah.app
			|
			|/** The recommended Java major version, generated from Gradle's JVM target. */
			|const val RECOMMENDED_JAVA_VERSION = ${javaVersion.get()}
			|
			""".trimMargin(),
		)
	}
}

val generateJavaVersion by tasks.registering(GenerateJavaVersion::class) {
	javaVersion.set((kotlin.targets.getByName("jvm") as KotlinJvmTarget).compilerOptions.jvmTarget.map { it.target })
	outputDirectory.set(layout.buildDirectory.dir("generated/sources/javaVersion"))
}

val mockkVersion: String by extra
val slf4jVersion: String by extra
val commonsIoVersion: String by extra
val commonsLang3Version: String by extra
val l2fprodVersion: String by extra

kotlin {

	sourceSets {

		val commonMain by getting {
			dependencies {
				implementation(project(":base"))
				implementation(project(":io"))
				implementation(project(":animation"))
				implementation(project(":draw"))
				implementation(project(":edit"))
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(project(":edit-test-util"))
				implementation(project(":app-test-util"))
			}
		}

		val jvmMain by getting {
			kotlin.srcDir(generateJavaVersion.flatMap { it.outputDirectory })

			dependencies {
				implementation("commons-cli:commons-cli:1.3.1")
			}
		}
	}
}
