package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.Editor
import com.ccfraser.muirwik.components.MGridProps
import com.ccfraser.muirwik.components.MGridSize
import com.ccfraser.muirwik.components.mGridItem
import com.ccfraser.muirwik.components.mTypography
import react.RBuilder
import styled.StyledElementBuilder
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

fun StyledElementBuilder<MGridProps>.propertyRow(labelKey: String, handler: (StyledElementBuilder<*>) -> Unit) {
	mGridItem(xs = MGridSize.cells4) {
		mTypography(Translations.getString(labelKey))
	}
	mGridItem(xs = MGridSize.cells8) {
		handler(this)
	}
}