package ch.scorpion.jabbah.edit.properties

import react.RBuilder
import kotlin.reflect.KClass

interface PropertyPageRenderer {
	fun render(bean: Any, builder: RBuilder)
}

class PropertyPageRendererRegistry {

	private val rendererMap: MutableMap<KClass<out Any>, PropertyPageRenderer> = mutableMapOf()

	fun render(bean: Any, builder: RBuilder) {
		rendererMap[bean::class]?.render(bean, builder)
	}

	fun register(clazz: KClass<out Any>, renderer: PropertyPageRenderer) {
		rendererMap[clazz] = renderer
	}
}