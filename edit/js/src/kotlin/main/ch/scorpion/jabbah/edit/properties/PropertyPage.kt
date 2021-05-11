package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.edit.Editor
import react.RBuilder
import kotlin.reflect.KClass

interface PropertyPageRenderer<T : Any> {
	fun render(bean: T, editor: Editor, builder: RBuilder)
}

class PropertyPageRendererRegistry {

	private val rendererMap: MutableMap<KClass<out Any>, PropertyPageRenderer<*>> = mutableMapOf()

	fun <T : Any> render(bean: T, editor: Editor, builder: RBuilder) {
		(rendererMap[bean::class] as PropertyPageRenderer<T>?)?.render(bean, editor, builder)
	}

	fun register(clazz: KClass<out Any>, renderer: PropertyPageRenderer<*>) {
		rendererMap[clazz] = renderer
	}
}