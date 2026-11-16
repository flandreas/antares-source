package io.antarescircuit.antares.web

import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.model.curve.AbstractCurveComponent
import io.antarescircuit.jabbah.edit.model.image.ImageComponent
import io.antarescircuit.jabbah.edit.model.polyline.PolylineComponent
import io.antarescircuit.jabbah.edit.model.rectangle.RectangularComponent
import io.antarescircuit.jabbah.edit.model.text.AbstractTextComponent
import io.antarescircuit.jabbah.edit.model.text.HorizontalAlignment
import io.antarescircuit.jabbah.edit.model.text.LabelComponent
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment

/** `EditProperties.filled()` */
internal fun <B : Component> WebPropertyBuilder<B>.filled() =
	bool("filled", "edit.property.filled", { it.filled }, { b, v -> b.filled = v }, group = WebGroups.APPEARANCE)

/** `EditProperties.stroked()` */
internal fun <B : Component> WebPropertyBuilder<B>.stroked() =
	bool("stroked", "edit.property.stroked", { it.stroked }, { b, v -> b.stroked = v }, group = WebGroups.APPEARANCE)

/** `EditProperties.styleType()` offering the user-choosable style types. */
internal fun <B : Component> WebPropertyBuilder<B>.styleType(bean: B) = choice(
	"styleType", "draw.styleType", DrawStyleModule.styleProvider.getChoosableStyleTypes(),
	{ it.styleType }, { b, v -> b.styleType = v!! },
	id = { it.name }, label = { it.description }, editable = !bean.fixStyleType, group = WebGroups.APPEARANCE,
)

/** `EditProperties.stroke()` */
internal fun <B : Component> WebPropertyBuilder<B>.customStroke() =
	stroke({ it.customStroke }, { b, v -> b.customStroke = v })

/** Figure components of the edit module (EditProperties based BeanInfos). */
internal fun WebPropertyRegistry.registerFigureProperties() {
	// PolylineComponentBeanInfo
	define<PolylineComponent> { c ->
		filled()
		styleType(c)
		customColor()
		customStroke()
		shadow()
		bool("arrow", "draw.property.polyline.arrow", { it.isArrow }, { b, v -> b.isArrow = v }, group = WebGroups.APPEARANCE)
		bool("horizontallyMirrored", "edit.property.mirrorHorizontally", { it.isHorizontallyMirrored }, { b, v -> b.isHorizontallyMirrored = v }, group = WebGroups.APPEARANCE)
		bool("verticallyMirrored", "edit.property.mirrorVertically", { it.isVerticallyMirrored }, { b, v -> b.isVerticallyMirrored = v }, group = WebGroups.APPEARANCE)
	}

	// AbstractCurveBeanInfo (quadratic and cubic curves)
	define<AbstractCurveComponent> { c ->
		filled()
		styleType(c)
		customColor()
		customStroke()
		shadow()
		bool("horizontallyMirrored", "edit.property.mirrorHorizontally", { it.isHorizontallyMirrored }, { b, v -> b.isHorizontallyMirrored = v }, group = WebGroups.APPEARANCE)
		bool("verticallyMirrored", "edit.property.mirrorVertically", { it.isVerticallyMirrored }, { b, v -> b.isVerticallyMirrored = v }, group = WebGroups.APPEARANCE)
	}

	// RectangularComponentBeanInfo (rectangle, rounded rectangle, ellipse, DIL case)
	define<RectangularComponent> { c ->
		filled()
		stroked()
		shadow()
		styleType(c)
		customColor()
		customStroke()
		translatable("text", "edit.property.text", { it.text }, { b, v -> b.text = v })
		description({ it.description }, { b, v -> b.description = v })
		enum("verticalAlignment", "edit.property.verticalAlignment", VerticalAlignment.entries, { it.verticalAlignment }, { b, v -> b.verticalAlignment = v }, group = WebGroups.APPEARANCE)
		enum("horizontalAlignment", "edit.property.horizontalAlignment", HorizontalAlignment.entries, { it.horizontalAlignment }, { b, v -> b.horizontalAlignment = v }, group = WebGroups.APPEARANCE)
	}

	// TextComponentBeanInfo (multi-line text)
	define<AbstractTextComponent> { c ->
		filled()
		stroked()
		styleType(c)
		customColor()
		translatable("text", "edit.property.text", { it.text }, { b, v -> b.text = v }, multiline = true)
		enum("horizontalAlignment", "edit.property.horizontalAlignment", HorizontalAlignment.entries, { it.horizontalAlignment }, { b, v -> b.horizontalAlignment = v }, group = WebGroups.APPEARANCE)
		bool("richText", "edit.property.text.isRichText", { it.isRichText }, { b, v -> b.isRichText = v })
	}

	// LabelComponentBeanInfo
	define<LabelComponent> { c ->
		styleType(c)
		customColor()
		translatable("text", "edit.property.text", { it.text }, { b, v -> b.text = v })
	}

	// ImageComponentBeanInfo (extends AbstractComponentBeanInfo, so without the rectangle's properties)
	define<ImageComponent> {
		remove(
			"filled", "stroked", "shadow", "styleType", "customColor", "customStroke", "text", "description",
			"verticalAlignment", "horizontalAlignment",
		)
		readOnly("name", "edit.property.name", { it.name?.value })
	}

	// GroupComponentBeanInfo has no own properties
}
