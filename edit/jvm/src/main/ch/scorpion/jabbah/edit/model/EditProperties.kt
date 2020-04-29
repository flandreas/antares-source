package ch.scorpion.jabbah.edit.model

import ch.scorpion.jabbah.draw.graphics.PredefinedColor
import ch.scorpion.jabbah.draw.graphics.PredefinedStroke
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.componentBeanProvider
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.edit.properties.PropertyImpl

object EditProperties {

	fun id(name: String = "filled", baseKey: String = "edit.property.id"): PropertyImpl<Int> {
		return PropertyImpl(name, baseKey, Int::class.java, componentBeanProvider)
	}

	fun filled(name: String = "filled", baseKey: String = "edit.property.filled"): PropertyImpl<Boolean> {
		return PropertyImpl(name, baseKey, Boolean::class.java, componentBeanProvider)
	}

	fun stroked(name: String = "stroked", baseKey: String = "edit.property.stroked"): PropertyImpl<Boolean> {
		return PropertyImpl(name, baseKey, Boolean::class.java, componentBeanProvider)
	}

	fun styleType(name: String = "styleType", baseKey: String = "draw.styleType"): PropertyImpl<StyleType> {
		return PropertyImpl(name, baseKey, StyleType::class.java, componentBeanProvider)
	}

	fun color(name: String = "customColor", baseKey: String = "edit.property.color"): PropertyImpl<PredefinedColor> {
		return PropertyImpl(name, baseKey, PredefinedColor::class.java, componentBeanProvider)
	}

	fun stroke(name: String = "customStroke", baseKey: String = "edit.property.stroke"): PropertyImpl<PredefinedStroke> {
		return PropertyImpl(name, baseKey, PredefinedStroke::class.java, componentBeanProvider)
	}

	fun text(name: String = "text", baseKey: String = "edit.property.text"): PropertyImpl<String> {
		return PropertyImpl(name, baseKey, String::class.java, componentBeanProvider)
	}

	fun multilineText(name: String = "text", baseKey: String = "edit.property.text"): PropertyImpl<TextProperty> {
		return PropertyImpl(name, baseKey, TextProperty::class.java, componentBeanProvider)
	}

	fun verticalAlignment(name: String = "alignment", baseKey: String = "edit.property.verticalAlignment"): PropertyImpl<VerticalAlignment> {
		return PropertyImpl(name, baseKey, VerticalAlignment::class.java, componentBeanProvider)
	}

	fun shadow(name: String = "shadow", baseKey: String = "edit.property.shadow"): PropertyImpl<Boolean> {
		return PropertyImpl(name, baseKey, Boolean::class.java, componentBeanProvider, setterPropertyName = "customShadow")
	}

	fun description(name: String = "description.translation", baseKey: String = "edit.property.description"): PropertyImpl<TranslatableText> {
		return PropertyImpl(name, baseKey, TranslatableText::class.java, componentBeanProvider)
	}

}