kotlin {
	sourceSets {
		val jvmMain by getting {
			dependencies {
				implementation("com.sparkjava:spark-core:2.9.3")
				implementation("com.sparkjava:spark-kotlin:1.0.0-alpha")
			}
		}
	}
}