package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.Editor
import com.ccfraser.muirwik.components.*
import react.RBuilder
import styled.StyledElementBuilder
import kotlin.reflect.KClass

interface PropertyPageRenderer<T : Any> {
	fun render(bean: T, editor: Editor, builder: RBuilder)
}

abstract class AbstractPropertyPageRenderer<T : Any> : PropertyPageRenderer<T> {
	override fun render(bean: T, editor: Editor, builder: RBuilder) {
		builder.run {
			mGridContainer(MGridSpacing.spacing1, alignItems = MGridAlignItems.center) {
				addProperties(bean, editor, this)
			}
		}
	}

	open fun addProperties(bean: T, editor: Editor, builder: StyledElementBuilder<MGridProps>) { }
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

fun StyledElementBuilder<MGridProps>.propertyRow(baseKey: String, handler: (StyledElementBuilder<MGridProps>) -> Unit) {
	mGridItem(xs = MGridSize.cells4) {
		mTypography(Translations.getString("$baseKey.name"))
	}
	mGridItem(xs = MGridSize.cells8) {
		handler(this)
	}
}