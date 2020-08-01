repositories {
	flatDir {
		dirs("$buildDir/../base/libs")
		dirs("$buildDir/../animation/libs")
		dirs("$buildDir/../io/libs")
		dirs("$buildDir/../draw/libs")
		dirs("$buildDir/../edit/libs")
	}
}

dependencies {
	project(":base")
	project(":animation")
	project(":io")
	project(":draw")
	project(":edit")

	nodeKotlin("jabbah:base-js:0.2.0")
	nodeKotlin("jabbah:animation-js:0.2.0")
	nodeKotlin("jabbah:io-js:0.2.0")
	nodeKotlin("jabbah:draw-js:0.2.0")
	nodeKotlin("jabbah:edit-js:0.2.0")
}

kt2ts {
	nodeSrcDirectory.set(
		project.layout.projectDirectory.dir(".")
	)
}
