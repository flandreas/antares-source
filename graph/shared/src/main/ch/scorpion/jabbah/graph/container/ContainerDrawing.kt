package ch.scorpion.jabbah.graph.container

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.dsl.Parser
import ch.scorpion.jabbah.base.dsl.ScopedSymbolTable
import ch.scorpion.jabbah.base.dsl.SemanticAnalyser
import ch.scorpion.jabbah.base.dsl.Symbol
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.graph.GraphStorable
import ch.scorpion.jabbah.graph.MetaGraphRepository
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.model.SubGraphPort
import ch.scorpion.jabbah.graph.model.vertice.DeepVerticeLink
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeImpl
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.port.PortView
import ch.scorpion.jabbah.graph.view.vertice.ControlViewVisibility
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import ch.scorpion.jabbah.io.*

/**
 * A [Drawing] that contains the graphical representation of a [SubGraphVertice]' outside view.
 */
class ContainerDrawing(
	name: String = Translations.getString("graph.name.unknown"),
	private val storableCreator: StorableCreator = IOModule.storableCreator,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val repository: MetaGraphRepository = LibraryModule.libraryHolder,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) : DrawingImpl<Component>(), Bean {

	companion object {
		private val LOG by logger(ContainerDrawing::class)
	}

	var model: SubGraphVertice = SubGraphVerticeImpl(name)
		private set

	/** Contains the script code that customized the visual look while execution mode.*/
	var execDrawScript = ScriptProperty()

	var controlViewVisibility = ControlViewVisibility.DEFAULT

	/** ---- [Any] */

	override fun toString(): String {
		if (model.getGraphIfPresent()?.name != null) {
			return Translations.getString("graph.property.ContainerDrawing", model.getGraphIfPresent()?.name!!)
		}
		return Translations.getString("graph.element.container.name")
	}

	/** ---- [DrawableContainer] interface */

	override fun add(drawable: Component, index: Int): DrawableContainer<Component> {
		super.add(drawable, index)
		if (resolvingFromStore) {
			return this
		}
		if (drawable is PortViewComponent<*>) {
			model.addPort(drawable.portView!!.port)
		}
		return this
	}

	override fun remove(drawable: Drawable): DrawableContainer<Component> {
		super.remove(drawable)
		if (drawable is PortViewComponent<*>) {
			model.removePort(drawable.portView!!.port)
		}
		return this
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeStorable("model", model)
		if (execDrawScript.isNotEmpty()) {
			writer.writeString("execDrawScript", execDrawScript.script!!)
		}
		if (controlViewVisibility != ControlViewVisibility.DEFAULT) {
			writer.writeString("controlViewVisibility", controlViewVisibility.customName)
		}
	}

	override fun read(reader: StoreReader) {
		model = reader.readStorable("model") as SubGraphVertice
		if (reader.hasAttribute("execDrawScript")) {
			execDrawScript = ScriptProperty(reader.readString("execDrawScript"))
		}
		if (reader.hasAttribute("controlViewVisibility")) {
			controlViewVisibility = ControlViewVisibility.withName(reader.readString("controlViewVisibility"))
		}
		super.read(reader)
	}

	/** ---- [ContainerDrawing] */

	/**
	 * Complete this [DrawableContainer] with information from the corresponding [Graph].
	 *
	 * This method is called by [GraphStorable] after reading from persistent store. It completes
	 * the model of this [DrawableContainer] with information from [Graph] objects, such as description
	 * of [GraphPort]s. These information are needed by [DrawableContainer] when filling
	 * [SubGraphVerticeView]s for that [Graph].
	 *
	 * Of course, this method is not cool. It conflicts with the design goal that [SubGraphVerticeView]
	 * can be constructed solely with information from [DrawableContainer], without using the [Graph].
	 * However, this goal may be out of reach anyway when [ContainerDrawing]s contain deeply linked [ControlView]s.
	 */
	fun completeFromGraph(graph: Graph) {
		model.getPorts().forEach {
			it.description = graph.getGraphPort<Any>(it.name!!)!!.description
		}
	}

	/**
	 * Checks if the [SubGraphPort]s of all [PortViewComponent]s is the same instance as
	 * the [SubGraphPort]s of the model [SubGraphVerticeViewImpl].
	 */
	fun areSubGraphPortsConsistent(): Boolean {
		for (c in getPortViewComponents()) {
			val outer = c.portView!!.port
			val inner = model.getPort<Any>(outer.name!!)
			if (outer !== inner) {
				LOG.warn("inconsistent SubGraphPort instances for port $outer")
				return false
			}
		}
		return true
	}

	/**
	 * Initializes this [ContainerDrawing] with a default [RectangleComponent] and a default [OriginIndicator].
	 * This is only needed when a new, fresh instance is created. It is not needed if this [ContainerDrawing] is
	 * read from persistent storage, as the [RectangleComponent] and the [OriginIndicator] are then read
	 * from storage.
	 */
	fun initialize() {
		add(RectangleComponent(140.0, 140.0, 56.0, 70.0))
		add(OriginIndicator(x = 140.0, y = 140.0))
	}

	/**
	 * Returns the [PortViewComponent] with the specified [Port] name.
	 * @param portName the name of [Port] of the requested [PortViewComponent].
	 */
	fun getPortViewComponent(portName: String): PortViewComponent<*>? {
		return getPortViewComponents().firstOrNull { it.portView!!.port.name == portName }
	}

	@Deprecated("Use querying with DeepVerticeLink")
	fun getControlViewComponent(controlId: String): ControlViewComponent? {
		return getControlViewComponents().firstOrNull { it.controlView.controlId == controlId }
	}

	fun getControlViewComponent(link: DeepVerticeLink): ControlViewComponent? {
		return getControlViewComponents().firstOrNull { it.controlModelLink == link }
	}

	fun createSubGraphVerticeView(): SubGraphVerticeView<SubGraphVerticeRef> {
		val model = SubGraphVerticeRef.fromSubGraphVertice(createSubGraphVertice(), repository)
		val view = SubGraphVerticeViewImpl(model, styleProvider, storableCreator, repository, eventBus)
		fillSubGraphVerticeView(view)
		view.controlViewVisibility = controlViewVisibility
		return view
	}

	/** Creates a copy of the [SubGraphVertice] model of this [ContainerDrawing].*/
	fun createSubGraphVertice(): SubGraphVertice {
		val subGraphVertice = StorableCloner.cloneUsingCreator(this.model, storableCreator)
		// The port descriptions are NOT copied when cloning, because they are not part of the persistent state
		// the Container's SubGraphVertice (see documentation of completeFromGraph()). Therefore, copy them now.
		subGraphVertice.getPorts().forEach {
			it.description = this.model.getPort<Any>(it.name!!).description
		}
		return subGraphVertice
	}

	/**
	 * Fills the specified [SubGraphVerticeView] with all visible [Drawable]s of this
	 * [ContainerDrawing], thus providing the look that has been designed by the library designer.
	 */
	fun fillSubGraphVerticeView(view: SubGraphVerticeView<SubGraphVerticeRef>) {
		LOG.trace("filling SubGraphVerticeViewRef name:${model.name}, uuid:${model.graphUUID}")

		val clonedDrawing = StorableCloner.cloneUsingCreator(this, storableCreator)
		val origin = clonedDrawing.getOriginIndicator().location

		for (comp in clonedDrawing.drawables) {
			comp.location = Point2D(comp.location.x - origin.x, comp.location.y - origin.y)
			if (comp is PortViewComponent<*>) {
				val portView = comp.portView as PortView<Any>
				view.addPortView(portView)
				try {
					portView.port = view.model.getPort(portView.port.name!!)
				} catch (e: NoSuchElementException) {
					LOG.error("SubGraphPort '${portView.port.name}' not found when filling SubGraphVerticeView for '${view.subGraphVertice!!.graphUUID}'")
					throw e
				}
			} else if (comp !is OriginIndicator) {
				view.addDrawable(comp)
			}
		}
	}

	fun createDrawSymbolScriptParser(program: String, semanticAnalyser: SemanticAnalyser?): Parser =
		BaseModule.parserFactory.create(program, BaseModule.semanticAnalyserFactory.create(createDrawExecSymbolParserSymbolTable()))

	private fun createDrawExecSymbolParserSymbolTable(): ScopedSymbolTable =
		ScopedSymbolTable("Context", scopeLevel = 0, enclosingScope = null).also {
			definePortNames(it)
			defineContextFunctions(it)
		}

	private fun definePortNames(symbolTable: ScopedSymbolTable) {
		getPortViewComponents()
			.filter { StringUtils.isNotBlank(it.port.name) }
			.forEach { symbolTable.define(Symbol(it.port.name!!)) }
	}

	private fun defineContextFunctions(symbolTable: ScopedSymbolTable) {
		DrawExecSymbolFunctions.defineIn(symbolTable)
	}

	private fun getPortViewComponents(): ImmutableList<PortViewComponent<*>> {
		return getDrawables { it is PortViewComponent<*> }.map { it as PortViewComponent<*> }.toImmutableList()
	}

	private fun getControlViewComponents(): ImmutableList<ControlViewComponent> {
		return getDrawables { it is ControlViewComponent }.map { it as ControlViewComponent }.toImmutableList()
	}

	private fun getOriginIndicator(): OriginIndicator {
		return getDrawables { it is OriginIndicator }.first() as OriginIndicator
	}
}