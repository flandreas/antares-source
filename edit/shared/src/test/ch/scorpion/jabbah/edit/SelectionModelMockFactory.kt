package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.edit.select.SelectionModelFactory
import kotlin.reflect.KClass

class SelectionModelMockFactory : SelectionModelFactory {

	override fun create(component: Component, strategy: SelectionDrawingStrategy): SelectionModel<Component> =
		SelectionModelMockBuilder().withComponent(component).build()

	override fun register(
		strategy: SelectionDrawingStrategy,
		componentClass: KClass<*>,
		factory: (Component) -> SelectionModel<Component>
	) { }
}