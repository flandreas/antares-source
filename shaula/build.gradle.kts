repositories {
	flatDir {
		dirs("$buildDir/../base/libs")
	}
}

dependencies {
	project(":base")
	nodeKotlin("jabbah:base-js:0.2.0")
}

kt2ts {
	nodeSrcDirectory.set(
		project.layout.projectDirectory.dir(".")
	)
}
