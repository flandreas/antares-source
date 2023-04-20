package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.*
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.container.DrawableBagInputEventHandler
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.Snapper
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.text.ScriptProperty
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Description
import ch.scorpion.jabbah.edit.model.text.description.Name
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.graph.GraphGlobalIdentityProvider
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.net.netview.NetViewImpl
import ch.scorpion.jabbah.graph.view.net.netview.NetViewPartitioner
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.scenario.ScenariosImpl
import ch.scorpion.jabbah.graph.view.usecase.UsecasesImpl
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView
import ch.scorpion.jabbah.io.*

/**
 * A standard implementation of the [GraphView] interface.
 */
open class GraphViewImpl(
	override var graph: Graph?,
	protected val eventBus: EventBus = BaseModule.eventBus,
) : DrawingImpl<GraphElementView<*>>(), GraphView, Bean {

	constructor() : this(TranslatableText(Translations.getString("graph.name.unknown")))
	constructor(name: TranslatableText, type: GraphType = GraphModelModule.graphTypeRegistry.default) : this(GraphModelModule.graphFactory.create(name, type))

	companion object {
		private val LOG by logger(GraphViewImpl::class)

		/** Use the same single [GraphViewInputEventHandler] instance for all [GraphViewImpl]. */
		var inputEventHandler: GraphViewInputEventHandler<*>? = null
	}

	/** Manages the [NetView]s for all [Net]s of the [Graph].*/
	private val netViewMap: MutableMap<Net<Any>, NetView<Any>> = mutableMapOf()

	/** ---- [Any] */

	override fun toString(): String {
		return graph?.name?.value ?: ""
	}

	/** ---- UI properties */

	override var name: String
		get() = graph!!.name.value
		set(value) {
			graph!!.name = Name(value)
		}

	val type: GraphType? get() = graph?.type

	@Suppress("unused")
	var translatableName: TranslatableText
		get() = graph!!.name.translation
		set(value) {
			graph!!.name = Name(value)
		}

	var description: TranslatableText
		get() = graph!!.description.translation
		set(value) {
			graph!!.description = Description(value)
		}

	@Suppress("unused") // Reflection
	open var overallPropagationDelay: Long?
		get() = graph!!.overallPropagationDelay
		set(value) {
			graph!!.overallPropagationDelay = value
		}

	var script: ScriptProperty
		get() = ScriptProperty(graph!!.script)
		set(value) {
			graph!!.script = value.script
		}

	@Suppress("unused")
	var purelyScripted: Boolean
		get() = graph!!.purelyScripted
		set(value) {
			graph!!.purelyScripted = value
		}

	/** ---- [GraphView] interface */

	override val allowMultipleOutputsPerNet: Boolean get() = false

	override var snapper: Snapper? = null

	override var scenarios: Scenarios = ScenariosImpl(this, eventBus)

	override var usecases: Usecases = UsecasesImpl(this, eventBus)

	override var currentScenario: Scenario? = null
		set(value) {
			if (value == field) {
				return
			}
			field = value
			if (value != null) {
				LOG.trace("Scenario '${value.name}' set in Graph '$name'")
			} else {
				LOG.trace("No current Scenario in Graph '$name'")
			}
			eventBus.post(ScenarioEvent(this, value))
		}

	override var currentScenarioStep: ScenarioStep? = null
		set(value) {
			if (value == field) {
				return
			}
			if (value != null && currentScenario == null) {
				throw IllegalStateException("ScenarioStep without Scenario")
			}
			val oldValue = field
			field = value
			if (value != null) {
				LOG.trace("ScenarioStep '${value.name}' of Scenario '${currentScenario!!.name}' set in Graph '$name'")
			} else {
				LOG.trace("No current ScenarioStep in Graph '$name'")
			}
			eventBus.post(ScenarioStepEvent(this, oldValue, value))
		}

	override fun bind(deep: Boolean) {
		for (graphElementView in drawables) {
			graphElementView.bind(this, deep)
		}
	}

	override fun executionStart(signalHandler: SignalHandler) {
		drawables.filterIsInstance<ActorView>().forEach { it.executionStarted(signalHandler) }
		scenarios.executionStart(this, signalHandler)
		usecases.executionStart(this, signalHandler)
	}

	override fun executionStop(signalHandler: SignalHandler) {
		drawables.filterIsInstance<ActorView>().forEach { it.executionStopped(signalHandler) }
	}

	override fun checkDesign(signalHandler: SignalHandler): Boolean {
		val issues = getDrawables { it.model.designError != null }
			.groupBy { it.model }
			.map { it.value.first() }
			.map {
				IssueImpl(
					severity = IssueSeverity.Error,
					name = Translations.getString("graph.designError.name"),
					description = it.model.designError?.description,
					origin = "${it.type} (${it.id})",
					context = null)
			}
		issues.forEach { eventBus.post(it) }
		return issues.isEmpty()
	}

	override fun cloneForExistingModel(model: Graph): GraphView {
		LOG.trace("clone '${model.name}'for existing model")
		val clone = StorableCloner.newClone(
			this,
			GraphGlobalIdentityProvider(model)
		)
		clone.graph = model
		return clone
	}

	override fun getVerticeView(name: String): VerticeView<*>? {
		return getDrawables { it is VerticeView<*> && it.model.name == name }.firstOrNull() as VerticeView<*>?
	}

	override fun getVerticeViews(): ImmutableList<VerticeView<*>> {
		return getDrawables { it is VerticeView<*> }.map { it as VerticeView<*> }.toImmutableList()
	}

	override fun getEdgeViews(): ImmutableList<EdgeView<*>> {
		return getDrawables { it is EdgeView<*> }.map { it as EdgeView<*> }.toImmutableList()
	}

	override fun getEdgeView(port: Port<*>, ignoredEdgeView: EdgeView<*>?): EdgeView<*>? {
		return getEdgeViews().firstOrNull {
			(ignoredEdgeView == null || ignoredEdgeView !== it) &&
			(it.origin?.port === port || it.destination?.port === port) }
	}

	override fun getNodeViews(): ImmutableList<NodeView<*>> =
		getDrawables { it is NodeView<*> }.map { it as NodeView<*> }.toImmutableList()

	override fun getGraphPortView(portName: String): GraphPortView<*>? {
		return getDrawable { it is GraphPortView<*> && it.model.name == portName } as GraphPortView<*>?
	}

	override fun getGraphPortViews(): ImmutableList<GraphPortView<*>> {
		return getDrawables { it is GraphPortView<*> }.map { it as GraphPortView<*> }.toImmutableList()
	}

	override fun getControlViewSources(): ImmutableList<ControlViewSource<Vertice>> {
		@Suppress("UNCHECKED_CAST")
		return getDrawables { it is ControlViewSource<*> } as ImmutableList<ControlViewSource<Vertice>>
	}

	override fun getControlViewSource(controlId: String): ControlViewSource<Vertice>? {
		@Suppress("UNCHECKED_CAST")
		return getDrawable { it is ControlViewSource<*> && it.controlId == controlId } as ControlViewSource<Vertice>?
	}

	override fun getElementViews(element: GraphElement): ImmutableList<GraphElementView<*>> {
		return getDrawables { it.model == element }
	}

	override fun getSubGraphVerticeViews(): ImmutableList<SubGraphVerticeView<*>> {
		return getDrawables { it is SubGraphVerticeView<*> }.map { it as SubGraphVerticeView<*> }.toImmutableList()
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		writer.writeStorables("netViews", netViewMap.values.iterator())
		if (!scenarios.isEmpty) {
			writer.writeStorable("scenarios", scenarios)
		}
		if (!usecases.isEmpty) {
			writer.writeStorable("usecases", usecases)
		}
		super.write(writer)
	}

	override fun read(reader: StoreReader) {
		netViewMap.clear()
		for (netView in reader.readStorables<Storable>("netViews")) {
			reader.requestResolution(this, Reference(
				name = "netView",
				additionalInfo = netView,
				resolveAfter = listOf(reader.getGlobalId(netView))
			))
		}
		if (reader.hasElement("scenarios")) {
			scenarios = reader.readStorable("scenarios") as Scenarios
			scenarios.graphView = this
		}
		if (reader.hasElement("usecases")) {
			usecases = reader.readStorable("usecases") as Usecases
			usecases.graphView = this
		}
		super.read(reader)
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		if (reference.name == "netView") {
			val resolvedNetView = reference.additionalInfo as NetView<Any>

			// If NetViewElements are resolved BEFORE a NetView, a corresponding dummy NetView already
			// exists in netViewMap and must be replaced by the one just read and resolved, because that one
			// holds the essential properties.
			netViewMap[resolvedNetView.net]?.let { existingNetView ->
				existingNetView.getElements().forEach { resolvedNetView.add(it) }
				netViewMap.remove(existingNetView.net)
			}

			addNetView(resolvedNetView)
		}
		super.resolve(reference, referenceResolver)
	}

	/** ---- [Drawable] interface */

	override fun accept(visitor: HierarchyVisitor): Boolean {
		if (visitor.visitEnter(this)) {
			if (!graph!!.accept(visitor)) {
				return visitor.visitLeave(this)
			}
			super.accept(visitor)
		}
		return visitor.visitLeave(this)
	}

	/** ---- [Drawing] interface */

	override fun dispose() {
		for (graphElementView in drawables) {
			graphElementView.dispose()
		}
		scenarios.dispose()
		usecases.dispose()
	}

	// ---- [DrawableContainerImpl] */

	/**
	 * Returns first all [VerticeView]s and then all other [GraphElementView]s, so that [EdgeView]s don't
	 * overwrite [VerticeView] boundaries when being connected to them.
	 * By doing so, we accept the negative side affect that simple [Component]s such as [RectangleComponent]s
	 * can never be drawn above [VerticeView]s, but it's more important that [RectangleComponent]s can be used
	 * as subsystem boundaries, which are always drawn above all other [GraphElementView]s.
	 */
	override fun drawablesInDrawingOrder(): ImmutableList<GraphElementView<*>> {
		val drawables = mutableListOf<GraphElementView<*>>()
		drawables.addAll(super.drawablesInDrawingOrder().filter { it !is VerticeView<*> })
		drawables.addAll(super.drawablesInDrawingOrder().filterIsInstance<VerticeView<*>>())
		return drawables.toImmutableList()
	}

	override fun provideInputEventHandler(): DrawableBagInputEventHandler<GraphElementView<*>, InputEventContext> {
		if (inputEventHandler == null) {
			inputEventHandler = GraphViewInputEventHandler<GraphElementView<*>>()
		}
		return inputEventHandler as DrawableBagInputEventHandler<GraphElementView<*>, InputEventContext>
	}

	/** Overridden in order to add the [GraphElement] to the [Graph] that this [GraphView] displays.*/
	override fun add(drawable: GraphElementView<*>, index: Int): DrawableContainer<GraphElementView<*>> {
		if (!contains(drawable)) {
			if (!resolvingFromStore) {
				graph?.add(drawable.model)
			}
			if (drawable is NetViewElement<*>) {
				addNetViewElement(drawable as NetViewElement<Any>)
			}
			return super.add(drawable, index)
		}
		return this
	}

	/** Overridden in order to remove the [GraphElement] from the [Graph] that this [GraphView] displays.*/
	override fun remove(drawable: Drawable): DrawableContainer<GraphElementView<*>> {
		if (graph != null && drawable is GraphElementView<*>) {
			if (getElementViews(drawable.model).size == 1) {
				graph!!.remove(drawable.model)
			}
			if (drawable is NetViewElement<*>) {
				removeNetViewElement(drawable as NetViewElement<Any>)
			}
			inputEventHandler?.graphElementViewRemoved()
		}
		return super<DrawingImpl>.remove(drawable)
	}

	override fun clear(): DrawableContainer<GraphElementView<*>> {
		super.clear()
		if (graph != null) {
			// graph should only be `null` during deserialization
			graph!!.clear()
		}
		return this
	}

	/** ---- [GraphViewImpl] */

	/** Only needed for testing.*/
	fun getNetViewCount(net: Net<*>): Int = netViewMap.filterKeys { it === net }.size

	/** Only needed for testing.*/
	fun getNetViewMap(): Map<Net<Any>, NetView<Any>> = netViewMap.toMap()

	private fun addNetView(netView: NetView<Any>) {
		if (!netViewMap.containsKey(netView.net)) {
			netViewMap[netView.net] = netView
		}
	}

	override val netViewsCount: Int get() = netViewMap.size

	override fun removeNetView(netView: NetView<*>) {
		netViewMap.remove(netView.net)
	}

	private fun addNetViewElement(elem: NetViewElement<Any>) {
		var netView = netViewMap[elem.net]
		if (netView == null) {
			netView = elem.netView?.clone() ?: NetViewImpl(elem.net!!)
			addNetView(netView)
		}
		netView.add(elem)
	}

	private fun removeNetViewElement(elem: NetViewElement<Any>) {
		val netView = netViewMap[elem.net]
		if (netView != null) {
			netView.remove(elem)
			if (netView.isEmpty) {
				removeNetView(netView)
			} else {
				if (elem is NodeView<*>) {
					val partitionedNetViews = NetViewPartitioner(netView).partition()
					if (partitionedNetViews.isNotEmpty()) {
						LOG.trace("Partitioned NetView into ${partitionedNetViews.size} new NetViews")
						removeNetView(netView)
						graph?.remove(netView.net)
						partitionedNetViews.forEach {
							graph?.add(it.net)
							addNetView(it)
						}
					}
				}
			}
		}
	}
}