import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

val mockkVersion: String by extra
val slf4jVersion: String by extra
val commonsIoVersion: String by extra
val commonsLang3Version: String by extra
val l2fprodVersion: String by extra

val batikVersion = "1.14"

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

		val jvmMain by getting {
			dependencies {
				implementation("org.apache.xmlgraphics:batik-anim:$batikVersion")
				implementation("org.apache.xmlgraphics:batik-awt-util:$batikVersion")
				implementation("org.apache.xmlgraphics:batik-bridge:$batikVersion")
				implementation("org.apache.xmlgraphics:batik-css:$batikVersion")
				implementation("org.apache.xmlgraphics:batik-dom:$batikVersion")
				implementation("org.apache.xmlgraphics:batik-ext:$batikVersion")
				implementation("org.apache.xmlgraphics:batik-gvt:$batikVersion")
				implementation("org.apache.xmlgraphics:batik-parser:$batikVersion")
				implementation("org.apache.xmlgraphics:batik-script:$batikVersion")
				implementation("org.apache.xmlgraphics:batik-svg-dom:$batikVersion")
				implementation("org.apache.xmlgraphics:batik-svggen:$batikVersion")
				implementation("org.apache.xmlgraphics:batik-transcoder:$batikVersion")
				implementation("org.apache.xmlgraphics:batik-util:$batikVersion")
				implementation("org.apache.xmlgraphics:batik-xml:$batikVersion")
			}
		}

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
