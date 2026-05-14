package io.antarescircuit.jabbah.edit.model

import io.antarescircuit.jabbah.base.dsl.ParserFactory
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.help.HelpId
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.graphics.PredefinedColor
import io.antarescircuit.jabbah.draw.graphics.PredefinedStroke
import io.antarescircuit.jabbah.draw.style.Stylable
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.componentBeanProvider
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.ScriptProperty
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment
import io.antarescircuit.jabbah.edit.model.text.description.BASE_KEY_DESCRIPTION
import io.antarescircuit.jabbah.edit.model.text.description.BASE_KEY_NAME
import io.antarescircuit.jabbah.edit.model.text.description.Description
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing
import io.antarescircuit.jabbah.edit.properties.MagnitudeValueProperty
import io.antarescircuit.jabbah.edit.properties.magnitude.SIUnit
import io.antarescircuit.jabbah.edit.properties.ScriptPropertySwing

object EditProperties {

	fun id(
		name: String = "id",
		baseKey: String = Component.BASE_KEY_ID,
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Int> {
		return CommandPropertySwing(name, baseKey, Int::class.java, beanProvider)
	}

	fun filled(
		name: String = "filled",
		baseKey: String = Stylable.BASE_KEY_FILLED,
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Boolean> {
		return CommandPropertySwing(name, baseKey, Boolean::class.java, beanProvider)
	}

	fun stroked(
		name: String = "stroked",
		baseKey: String = Stylable.BASE_KEY_STROKED,
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
		baseKey: String = Stylable.BASE_KEY_CUSTOM_COLOR,
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<PredefinedColor> {
		return CommandPropertySwing(name, baseKey, PredefinedColor::class.java, beanProvider)
	}

	fun stroke(
		name: String = "customStroke",
		baseKey: String = Stylable.BASE_KEY_CUSTOM_STROKE,
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
		name: String = "verticalAlignment",
		baseKey: String = "edit.property.verticalAlignment",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<VerticalAlignment> {
		return CommandPropertySwing(name, baseKey, VerticalAlignment::class.java, beanProvider)
	}

	fun horizontalAlignment(
		name: String = "horizontalAlignment",
		baseKey: String = "edit.property.horizontalAlignment",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<HorizontalAlignment> {
		return CommandPropertySwing(name, baseKey, HorizontalAlignment::class.java, beanProvider)
	}

	fun richText(
		name: String = "richText",
		baseKey: String = "edit.property.text.isRichText",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Boolean> {
		return CommandPropertySwing(name, baseKey, Boolean::class.java, beanProvider)
	}

	fun shadow(
		name: String = "shadow",
		baseKey: String = Stylable.BASE_KEY_SHADOW,
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Boolean> {
		return CommandPropertySwing(name, baseKey, Boolean::class.java, beanProvider, setterPropertyName = "customShadow")
	}

	fun name(
		name: String = "name",
		baseKey: String = BASE_KEY_NAME,
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Name> {
		return CommandPropertySwing(name, baseKey, Name::class.java, beanProvider)
	}

	fun untranslatableName(name: String = "name"): CommandPropertySwing<String> =
		CommandPropertySwing(name, BASE_KEY_NAME, String::class.java, componentBeanProvider)

	fun description(
		name: String = "description",
		baseKey: String = BASE_KEY_DESCRIPTION,
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Description> {
		return CommandPropertySwing(name, baseKey, Description::class.java, beanProvider)
	}

	fun orientation(
		name: String = "orientation",
		baseKey: String = Component.BASE_KEY_ORIENTATION,
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Direction> {
		return CommandPropertySwing(name, baseKey, Direction::class.java, beanProvider)
	}

	fun size(
		name: String = "size",
		baseKey: String = Size.BASE_KEY_SIZE,
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Size> {
		return CommandPropertySwing(name, baseKey, Size::class.java, beanProvider)
	}

	fun script(
		name: String,
		baseKey: String,
		beanProvider: BeanProvider = componentBeanProvider,
		parserFactory: ParserFactory? = BaseModule.parserFactory,
		helpId: HelpId? = null
	): CommandPropertySwing<ScriptProperty> {
		return ScriptPropertySwing(name, baseKey, beanProvider, parserFactory, helpId)
	}

	fun border(
		name: String = "hasBorder",
		baseKey: String = "edit.property.hasBorder",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Boolean> {
		return CommandPropertySwing(name, baseKey, Boolean::class.java, beanProvider)
	}

	fun horizontallyMirrored(
		name: String = "horizontallyMirrored",
		baseKey: String = "edit.property.mirrorHorizontally",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Boolean> {
		return CommandPropertySwing(name, baseKey, Boolean::class.java, beanProvider)
	}

	fun verticallyMirrored(
		name: String = "verticallyMirrored",
		baseKey: String = "edit.property.mirrorVertically",
		beanProvider: BeanProvider = componentBeanProvider
	): CommandPropertySwing<Boolean> {
		return CommandPropertySwing(name, baseKey, Boolean::class.java, beanProvider)
	}

	private fun magnitudeValue(
		name: String,
		baseKey: String,
		beanProvider: BeanProvider = componentBeanProvider,
		vararg units: SIUnit
	): MagnitudeValueProperty = MagnitudeValueProperty(name, baseKey, beanProvider, *units)

	fun ohm(
		name: String,
		baseKey: String,
		beanProvider: BeanProvider = componentBeanProvider
	): MagnitudeValueProperty = magnitudeValue(name, baseKey, beanProvider, SIUnit.Ohm)

	fun farad(
		name: String,
		baseKey: String,
		beanProvider: BeanProvider = componentBeanProvider
	): MagnitudeValueProperty = magnitudeValue(name, baseKey, beanProvider, SIUnit.Farad)

	fun henry(
		name: String,
		baseKey: String,
		beanProvider: BeanProvider = componentBeanProvider
	): MagnitudeValueProperty = magnitudeValue(name, baseKey, beanProvider, SIUnit.Henry)

	fun volt(
		name: String,
		baseKey: String,
		beanProvider: BeanProvider = componentBeanProvider
	): MagnitudeValueProperty = magnitudeValue(name, baseKey, beanProvider, SIUnit.Volt)

	fun ampere(
		name: String,
		baseKey: String,
		beanProvider: BeanProvider = componentBeanProvider
	): MagnitudeValueProperty = magnitudeValue(name, baseKey, beanProvider, SIUnit.Ampere)

	fun periodOrFrequency(
		name: String,
		baseKey: String,
		beanProvider: BeanProvider = componentBeanProvider
	): MagnitudeValueProperty = magnitudeValue(name, baseKey, beanProvider, SIUnit.Second, SIUnit.Hertz)

	fun time(
		name: String,
		baseKey: String,
		beanProvider: BeanProvider = componentBeanProvider
	): MagnitudeValueProperty = magnitudeValue(name, baseKey, beanProvider, SIUnit.Second)

}