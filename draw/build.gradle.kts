import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

val mockkVersion: String by extra
val slf4jVersion: String by extra
val commonsIoVersion: String by extra
val commonsLang3Version: String by extra
val l2fprodVersion: String by extra

kt2ts {
	classPatterns.set(listOf(
		"ch.scorpion.jabbah.draw.module.DrawModule",
		"ch.scorpion.jabbah.draw.Canvas",
		"ch.scorpion.jabbah.draw.View",
		"ch.scorpion.jabbah.draw.view.CanvasJs"
	))
}

kotlin {

	sourceSets {

		val commonMain by getting {
			dependencies {
				implementation(project(":base"))
				implementation(project(":animation"))
			}
		}

		val commonDemo by creating {
			kotlin.srcDir("shared/src/demo")
			dependencies {
				dependsOn(commonMain)
			}
		}

		val jvmMain by getting

		targets.getByName("jvm").compilations {
			val main by getting
			val demo by creating {
				kotlinOptions {
					(this as KotlinJvmOptions).jvmTarget = JavaVersion.VERSION_1_8.toString()
				}
				defaultSourceSet {
					kotlin.srcDir("jvm/src/demo")
					dependencies {
						dependsOn(commonMain)
						dependsOn(commonDemo)
						dependsOn(jvmMain)
						implementation(main.compileDependencyFiles + main.output.classesDirs)
					}
				}
			}

			tasks.create<JavaExec>("demo") {
				setMain("ch.scorpion.jabbah.draw.hellographics.HelloGraphicsJvm")
				classpath = demo.output.classesDirs + main.compileDependencyFiles + commonMain.resources.sourceDirectories
			}
		}
	}
}
