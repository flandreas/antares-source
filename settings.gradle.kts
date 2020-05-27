pluginManagement {
	resolutionStrategy {
		eachPlugin {
			if (requested.id.id == "kotlin2js") {
				useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
			}
		}
	}
}
rootProject.name = "jabbah"

include("base")
include("io")
include("animation")
include("draw")
include("edit")
include("execution")
include("app")
include("graph")
include("antares")

enableFeaturePreview("GRADLE_METADATA")
