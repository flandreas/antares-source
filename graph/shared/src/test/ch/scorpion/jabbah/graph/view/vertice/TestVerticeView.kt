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

	companion object {
		const val DEF_SIZE = 20

		fun createEastOutputVerticeView(name: String, x: Int, y: Int): TestVerticeView =
			TestVerticeView(name = name, loc = Point2D(x, y), inputDirection = Direction.WEST, outputDirection = Direction.EAST, width = DEF_SIZE)

		fun createSouthInputVerticeView(name: String, x: Int, y: Int): TestVerticeView =
			TestVerticeView(name, loc = Point2D(x, y), inputDirection = Direction.SOUTH, outputDirection = Direction.NORTH, width = 0, height = DEF_SIZE)
	}

	init {
		location = loc
		modelExchanged(null)
	}

	override fun toString(): String = "TestVerticeView ${model.name}"

	override fun modelExchanged(oldModel: TestVertice?) {
		super.modelExchanged(oldModel)
		addPortView(TestPortView(model.getInput(), inputDirection, PortLabelPosition.INTERNAL, portViewLength, Point2D.ZERO))
		addPortView(TestPortView(model.getOutput(), outputDirection, PortLabelPosition.INTERNAL, portViewLength, Point2D(width, -height)))
	}

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt("width", widthInt)
		writer.writeInt("height", heightInt)
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		width = reader.readInt("width").toDouble()
		height = reader.readInt("height").toDouble()
	}
}