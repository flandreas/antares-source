kotlin {
	sourceSets {
		val jvmMain by getting {
			dependencies {
				api(project(":graph"))
				implementation("commons-cli:commons-cli:1.3.1")
				implementation("com.sparkjava:spark-core:2.9.3")
				implementation("com.sparkjava:spark-kotlin:1.0.0-alpha")
			}
		}
	}
}