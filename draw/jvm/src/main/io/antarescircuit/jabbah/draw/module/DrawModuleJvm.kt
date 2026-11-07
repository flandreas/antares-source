package io.antarescircuit.jabbah.draw.module

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.module.BaseModuleJvm
import io.antarescircuit.jabbah.draw.graphics.ImageLoaderJvm
import io.antarescircuit.jabbah.draw.graphics.RasterImageJvm

/** JVM rendering and image services, also usable without the desktop UI. */
object DrawModuleJvm : AbstractModule() {

	override fun initialize() {
		BaseModuleJvm.require()
		DrawModule.rasterImageFactory = { width, height -> RasterImageJvm(width, height) }
		DrawModule.imageLoader = ImageLoaderJvm()
		DrawModule.require()
	}

	override fun resetDependencies() {
		BaseModuleJvm.reset()
		DrawModule.reset()
	}
}
