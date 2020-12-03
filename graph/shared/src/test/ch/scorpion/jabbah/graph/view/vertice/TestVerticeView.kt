package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.TestVertice
import ch.scorpion.jabbah.graph.view.port.TestPortView
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * Contains an input [TestPortView] directed to [Direction.WEST] and an output [TestPortView]
 * directed to [Direction.EAST].
 */
class TestVerticeView(
	name: String = "",
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	vertice: TestVertice = TestVertice(name = name),
	loc: Point2D = Point2D.ZERO,
	private var inputDirection: Direction = Direction.WEST,
	private var outputDirection: Direction = Direction.EAST,
	private var portViewLength: Int? = null,
	width: Int = 0,
	height: Int = 0
) : AbstractRectangularVerticeView<TestVertice>(styleProvider, vertice, loc.x, loc.y, width.toDouble(), height.toDouble()) {

	init {
		location = loc
		modelExchanged(null)
	}

	override fun toString(): String {
		return "TestVerticeView ${model.name}"
	}

	override fun modelExchanged(oldModel: TestVertice?) {
		super.modelExchanged(oldModel)
		addPortView(TestPortView(model.getInput<Boolean>(), inputDirection, PortLabelPosition.INTERNAL, portViewLength, Point2D.ZERO))
		addPortView(TestPortView(model.getOutput<Boolean>(), outputDirection, PortLabelPosition.INTERNAL, portViewLength, Point2D(width, 0.0)))
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("width", widthInt)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		width = reader.readInt("width").toDouble()
	}
}