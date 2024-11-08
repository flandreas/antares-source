package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawTheme
import ch.scorpion.jabbah.draw.style.Themes

class AddIcon(override val dim: Dimension2D) : Icon {

	override fun draw(context: DrawContext, location: Point2D) {
		context.g.color = context.choose(Themes.get<DrawTheme>().figure.color).foregroundColor
		context.g.drawOval(location.x, location.y, dim.width, dim.height)
		context.g.drawLine(
			location.x + dim.width / 3, location.y + dim.height / 2,
			location.x + 2 * dim.width / 3, location.y + dim.height / 2)
		context.g.drawLine(
			location.x + dim.width / 2, location.y + dim.height / 3,
			location.x + dim.width / 2, location.y + 2 * dim.height / 3)
	}
}

class RemoveIcon(override val dim: Dimension2D) : Icon {

	override fun draw(context: DrawContext, location: Point2D) {
		context.g.color = context.choose(Themes.get<DrawTheme>().figure.color).foregroundColor
		context.g.drawOval(location.x, location.y, dim.width, dim.height)
		context.g.drawLine(
			location.x + dim.width / 3, location.y + dim.height / 2,
			location.x + 2 * dim.width / 3, location.y + dim.height / 2)
	}
}

class KnobIcon(override val dim: Dimension2D) : Icon {

	override fun draw(context: DrawContext, location: Point2D) {
		context.g.color = context.choose(Themes.get<DrawTheme>().figure.color).foregroundColor
		context.translated(location) {
			it.g.drawOval(0.0, 0.0, dim.width, dim.height)
			it.g.fillOval(dim.width / 2 - 3, dim.height / 2 - 3, 6.0, 6.0)
			it.g.drawLine(dim.width / 2, dim.height / 2, dim.width / 2 + 4, dim.height / 2  - 4)
		}
	}
}