package io.antarescircuit.jabbah.edit.model.rectangle

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.model.image.ImageComponent
import io.antarescircuit.jabbah.edit.select.EditSelectModule

/**
 * Module definitions for the [io.antarescircuit.jabbah.edit.model.rectangle] package.
 */
object EditModelRectangleModule : AbstractModule() {

	override fun initialize() {
		EditSelectModule.selectionModelFactory.register(
			SelectionDrawingStrategy.BELOW,
			RectangleComponent::class
		) { RectangularBelowSelectionModel(it as AbstractRectangularComponent) }

		EditSelectModule.selectionModelFactory.register(
			SelectionDrawingStrategy.ABOVE,
			RectangleComponent::class
		) { RectangularHandleSelectionModel(it as AbstractRectangularComponent) }

		EditSelectModule.selectionModelFactory.register(
			SelectionDrawingStrategy.REPLACE,
			RectangleComponent::class
		) { RectangularReplaceSelectionModel(it as AbstractRectangularComponent) }

		EditSelectModule.selectionModelFactory.register(
			SelectionDrawingStrategy.BELOW,
			EllipseComponent::class
		) { RectangularBelowSelectionModel(it as AbstractRectangularComponent) }

		EditSelectModule.selectionModelFactory.register(
			SelectionDrawingStrategy.ABOVE,
			EllipseComponent::class
		) { RectangularHandleSelectionModel(it as AbstractRectangularComponent) }

		EditSelectModule.selectionModelFactory.register(
			SelectionDrawingStrategy.REPLACE,
			EllipseComponent::class
		) { RectangularReplaceSelectionModel(it as AbstractRectangularComponent) }

		EditSelectModule.selectionModelFactory.register(
			SelectionDrawingStrategy.BELOW,
			RoundRectangleComponent::class
		) { RectangularBelowSelectionModel(it as AbstractRectangularComponent) }

		EditSelectModule.selectionModelFactory.register(
			SelectionDrawingStrategy.ABOVE,
			RoundRectangleComponent::class
		) { RectangularHandleSelectionModel(it as AbstractRectangularComponent) }

		EditSelectModule.selectionModelFactory.register(
			SelectionDrawingStrategy.REPLACE,
			RoundRectangleComponent::class
		) { RectangularReplaceSelectionModel(it as AbstractRectangularComponent) }

		EditSelectModule.selectionModelFactory.register(SelectionDrawingStrategy.BELOW, ImageComponent::class) { RectangularBelowSelectionModel(it as AbstractRectangularComponent) }
		EditSelectModule.selectionModelFactory.register(SelectionDrawingStrategy.REPLACE, ImageComponent::class) { RectangularReplaceSelectionModel(it as AbstractRectangularComponent) }
		EditSelectModule.selectionModelFactory.register(SelectionDrawingStrategy.ABOVE, ImageComponent::class) { RectangularHandleSelectionModel(it as AbstractRectangularComponent) }

	}

	override fun resetDependencies() {}
}