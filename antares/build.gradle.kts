import org.gradle.internal.os.OperatingSystem

val version_project: String by extra
val mockkVersion: String by extra
val slf4jVersion: String by extra
val commonsIoVersion: String by extra
val commonsLang3Version: String by extra
val l2fprodVersion: String by extra

repositories {
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
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

		val jvmTest by getting {
			dependencies {
				implementation(project(":base-ui"))
				implementation(project(":draw-ui"))
				implementation(project(":edit-ui"))
				implementation(project(":execution-ui"))
				implementation(project(":app-ui"))
				implementation(project(":graph-ui"))
				implementation(project(":antares-ui"))
			}
		}
	}
}

// Compatibility entry points. Desktop packaging and installer implementation live in antares-ui.
listOf(
	"shadowCreate",
	"copySplash",
	"packageMac",
	"signDmgMac",
	"notarizeMac",
	"stapleMacNotarization",
	"distributeMac",
	"distributeWindows",
	"distributeLinux",
).forEach { taskName ->
	tasks.register(taskName) {
		group = "distribution"
		description = "Compatibility alias for :antares-ui:$taskName"
		dependsOn(":antares-ui:$taskName")
	}
}

// Keep JavaExec here so existing :antares:run --args=... invocations remain valid.
tasks.register<JavaExec>("run") {
	group = "application"
	description = "Run the desktop application built by antares-ui"
	dependsOn(":antares-ui:shadowCreate")
	classpath = files(project(":antares-ui").layout.buildDirectory.file("libs/antares-$version_project-all.jar"))
	mainClass.set("io.antarescircuit.antares.AntaresSwing")
}
