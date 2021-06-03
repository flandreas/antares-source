package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.draw.graphics.PredefinedStroke
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.text.*
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing

object EditProperties {

	fun id(
		name: String = "id",
		baseKey: String = "edit.property.id",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Int> {
		return CommandPropertySwing(name, baseKey, Int::class.java, beanProvider)
	}

	fun filled(
		name: String = "filled",
		baseKey: String = "edit.property.filled",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Boolean> {
		return CommandPropertySwing(name, baseKey, Boolean::class.java, beanProvider)
	}

	fun stroked(
		name: String = "stroked",
		baseKey: String = "edit.property.stroked",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Boolean> {
		return CommandPropertySwing(name, baseKey, Boolean::class.java, beanProvider)
	}

	fun styleType(
		name: String = "styleType",
		baseKey: String = "draw.styleType",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<StyleType> {
		return CommandPropertySwing(name, baseKey, StyleType::class.java, beanProvider)
	}

	fun color(
		name: String = "customColor",
		baseKey: String = "edit.property.color",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<PredefinedColor> {
		return CommandPropertySwing(name, baseKey, PredefinedColor::class.java, beanProvider)
	}

	fun stroke(
		name: String = "customStroke",
		baseKey: String = "edit.property.stroke",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<PredefinedStroke> {
		return CommandPropertySwing(name, baseKey, PredefinedStroke::class.java, beanProvider)
	}

	fun text(
		name: String = "text",
		baseKey: String = "edit.property.text",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<String> {
		return CommandPropertySwing(name, baseKey, String::class.java, beanProvider)
	}

	fun translatableText(
		name: String = "text",
		baseKey: String = "edit.property.text",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<TranslatableText> {
		return CommandPropertySwing(name, baseKey, TranslatableText::class.java, beanProvider)
	}

	fun verticalAlignment(
		name: String = "alignment",
		baseKey: String = "edit.property.verticalAlignment",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<VerticalAlignment> {
		return CommandPropertySwing(name, baseKey, VerticalAlignment::class.java, beanProvider)
	}

	fun horizontalAlignment(
		name: String = "alignment",
		baseKey: String = "edit.property.horizontalAlignment",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<HorizontalAlignment> {
		return CommandPropertySwing(name, baseKey, HorizontalAlignment::class.java, beanProvider)
	}

	fun shadow(
		name: String = "shadow",
		baseKey: String = "edit.property.shadow",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Boolean> {
		return CommandPropertySwing(name, baseKey, Boolean::class.java, beanProvider, setterPropertyName = "customShadow")
	}

	fun name(
		name: String = "name",
		baseKey: String = "edit.property.name",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Name> {
		return CommandPropertySwing(name, baseKey, Name::class.java, beanProvider)
	}

	fun untranslatableName(name: String = "name"): CommandPropertySwing<String> =
		CommandPropertySwing(name, "edit.property.name", String::class.java, componentBeanProvider)

	fun description(
		name: String = "description",
		baseKey: String = "edit.property.description",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Description> {
		return CommandPropertySwing(name, baseKey, Description::class.java, beanProvider)
	}

	fun orientation(
		name: String = "orientation",
		baseKey: String = "edit.property.Component.orientation",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Direction> {
		return CommandPropertySwing(name, baseKey, Direction::class.java, beanProvider)
	}

	fun size(
		name: String = "size",
		baseKey: String = "edit.property.size",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Size> {
		return CommandPropertySwing(name, baseKey, Size::class.java, beanProvider)
	}

	fun script(
		name: String,
		baseKey: String,
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<ScriptProperty> {
		return CommandPropertySwing(name, baseKey, ScriptProperty::class.java, beanProvider, interactive = true)
	}
}