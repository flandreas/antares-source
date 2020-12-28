package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.draw.graphics.PredefinedStroke
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.text.*
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.edit.properties.PropertyImpl

object EditProperties {

	fun id(
		name: String = "id",
		baseKey: String = "edit.property.id",
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<Int> {
		return PropertyImpl(name, baseKey, Int::class.java, beanProvider)
	}

	fun filled(
		name: String = "filled",
		baseKey: String = "edit.property.filled",
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<Boolean> {
		return PropertyImpl(name, baseKey, Boolean::class.java, beanProvider)
	}

	fun stroked(
		name: String = "stroked",
		baseKey: String = "edit.property.stroked",
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<Boolean> {
		return PropertyImpl(name, baseKey, Boolean::class.java, beanProvider)
	}

	fun styleType(
		name: String = "styleType",
		baseKey: String = "draw.styleType",
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<StyleType> {
		return PropertyImpl(name, baseKey, StyleType::class.java, beanProvider)
	}

	fun color(
		name: String = "customColor",
		baseKey: String = "edit.property.color",
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<PredefinedColor> {
		return PropertyImpl(name, baseKey, PredefinedColor::class.java, beanProvider)
	}

	fun stroke(
		name: String = "customStroke",
		baseKey: String = "edit.property.stroke",
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<PredefinedStroke> {
		return PropertyImpl(name, baseKey, PredefinedStroke::class.java, beanProvider)
	}

	fun text(
		name: String = "text",
		baseKey: String = "edit.property.text",
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<String> {
		return PropertyImpl(name, baseKey, String::class.java, beanProvider)
	}

	fun translatableText(
		name: String = "text",
		baseKey: String = "edit.property.text",
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<TranslatableText> {
		return PropertyImpl(name, baseKey, TranslatableText::class.java, beanProvider)
	}

	fun verticalAlignment(
		name: String = "alignment",
		baseKey: String = "edit.property.verticalAlignment",
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<VerticalAlignment> {
		return PropertyImpl(name, baseKey, VerticalAlignment::class.java, beanProvider)
	}

	fun horizontalAlignment(
		name: String = "alignment",
		baseKey: String = "edit.property.horizontalAlignment",
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<HorizontalAlignment> {
		return PropertyImpl(name, baseKey, HorizontalAlignment::class.java, beanProvider)
	}

	fun shadow(
		name: String = "shadow",
		baseKey: String = "edit.property.shadow",
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<Boolean> {
		return PropertyImpl(name, baseKey, Boolean::class.java, beanProvider, setterPropertyName = "customShadow")
	}

	fun name(
		name: String = "name",
		baseKey: String = "edit.property.name",
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<TranslatableText> {
		return PropertyImpl(name, baseKey, TranslatableText::class.java, beanProvider)
	}

	fun description(
		name: String = "description",
		baseKey: String = "edit.property.description",
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<Description> {
		return PropertyImpl(name, baseKey, Description::class.java, beanProvider)
	}

	fun orientation(
		name: String = "orientation",
		baseKey: String = "edit.property.Component.orientation",
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<Direction> {
		return PropertyImpl(name, baseKey, Direction::class.java, beanProvider)
	}

	fun size(
		name: String = "size",
		baseKey: String = "edit.property.size",
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<Size> {
		return PropertyImpl(name, baseKey, Size::class.java, beanProvider)
	}

	fun script(
		name: String,
		baseKey: String,
		beanProvider: BeanProvider = componentBeanProvider
	): PropertyImpl<ScriptProperty> {
		return PropertyImpl(name, baseKey, ScriptProperty::class.java, beanProvider, interactive = true)
	}
}