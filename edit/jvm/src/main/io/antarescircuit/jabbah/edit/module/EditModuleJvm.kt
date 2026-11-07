package io.antarescircuit.jabbah.edit.module

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.module.DrawModuleJvm
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.model.text.*
import io.antarescircuit.jabbah.io.IOModuleJvm

/** JVM editor model setup. Swing editors and factories live in edit-ui. */
object EditModuleJvm : AbstractModule() {

	override fun initialize() {
		DrawModuleJvm.require()
		IOModuleJvm.require()
		EditModule.require()
		EditModelTextModule.textComponentFactory = object : TextComponentFactory {
			override fun create(
				text: TranslatableText,
				location: Point2D,
				styleType: StyleType,
				styleProvider: StyleProvider,
			): TextComponent = SimpleTextComponent(text, location, styleType, styleProvider)
		}
	}

	override fun resetDependencies() {
		DrawModuleJvm.reset()
		IOModuleJvm.reset()
		EditModule.reset()
	}
}
