kt2ts {
	classPatterns.set(listOf(
		"ch.scorpion.jabbah.base.StringUtils"
	))
}

tasks.named("compileKotlinJs") {
	this as org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
	kotlinOptions.moduleKind = "commonjs"
	kotlinOptions.noStdlib = true
	kotlinOptions.outputFile = "${project.buildDir.path}/classes/kotlin/js/main/jabbah-${project.name}.js"
}
