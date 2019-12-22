buildscript {

	val kotlinVersion: String by extra

	repositories {
		jcenter()
	}

	dependencies {
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
	}

	allprojects {

		repositories {
			mavenCentral()
			jcenter()
			flatDir {
				dirs("../lib")
			}
		}
	}

	tasks.register<Delete>("clean") {
		delete(rootProject.buildDir)
	}
}