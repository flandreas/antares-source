package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.draw.graphics.PredefinedColorProvider
import ch.scorpion.jabbah.draw.graphics.PredefinedColorRepository
import ch.scorpion.jabbah.edit.*
import com.ccfraser.muirwik.components.mSelect
import com.ccfraser.muirwik.components.menu.mMenuItem
import com.ccfraser.muirwik.components.targetValue
import react.*

fun predefinedColorField(
	displayName: String? = null,
	provider: PredefinedColorProvider
): FunctionalComponent<PropertyProps<PredefinedColor?>> = functionalComponent(displayName) { props ->
	var oldValue = props.getter(props.beanProvider(props.editor, props.beanIds))
	val (value, setValue) = useState(oldValue)

	mSelect(value?.name ?: "none", onChange = { e, _ ->
		val newValue = provider.withIdName(e.targetValue as String)
		setValue(newValue)
		submitCommand(props, newValue)
		oldValue = newValue
	}) {
		mMenuItem(Translations.getString("edit.style.property.fromStyle.name"), value = "none")
		provider.provideAll().forEach {
			mMenuItem(it.description, value = it.name)
		}
	}
}

val jmPredefinedColorField = predefinedColorField("PredefinedColor", PredefinedColorRepository)

fun RBuilder.jmPredefinedColorField(
	editor: Editor,
	getter: PropertyGetter<PredefinedColor?>,
	setter: PropertySetter<PredefinedColor?>,
	beanId: Int,
	beanProvider: BeanProvider = componentBeanProvider,
	handler: PropertyProps<PredefinedColor?>.() -> Unit = {}
) = child(jmPredefinedColorField) {
	attrs {
		this.editor = editor
		this.getter = getter
		this.setter = setter
		this.beanIds = listOf(beanId)
		this.beanProvider = beanProvider
		handler()
	}
}
