package ch.scorpion.jabbah.graph.view.graph

import ch.scorpion.jabbah.base.HierarchyVisitor
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.collection.ConcatIterator
import ch.scorpion.jabbah.base.collection.ImmutableList
import ch.scorpion.jabbah.base.collection.toImmutableList
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.container.DrawableContainerInputEventHandler
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.Snapper
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.execution.scheduler.Scheduler
import ch.scorpion.jabbah.execution.scheduler.SchedulerActivationStateEvent
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.graph.model.graph.GraphReferenceResolver
import ch.scorpion.jabbah.graph.model.module.GraphModelModule
import ch.scorpion.jabbah.graph.view.*
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewFactory
import ch.scorpion.jabbah.graph.view.scenario.ScenariosImpl
import ch.scorpion.jabbah.io.*
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.execution.issue.IssueImpl
import ch.scorpion.jabbah.execution.issue.IssueSeverity
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.view.VerticeView
import ch.scorpion.jabbah.graph.view.connect.*
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import ch.scorpion.jabbah.graph.view.net.netview.NetViewImpl
import ch.scorpion.jabbah.graph.view.usecase.UsecasesImpl
import ch.scorpion.jabbah.graph.view.vertice.SubGraphVerticeView

/**
 * A standard implementation of the [GraphView] interface.
 */
class GraphViewImpl<T : GraphElementView<*>>(
	override var graph: Graph?,
	private val storableCloner: StorableCloner = IOModule.storableClonerProvider.invoke(),
	private val outputToInputConnector: OutputToInputConnector = GraphViewModule.outputToInputConnector,
	private val inputToOutputOrEdgeConnector: InputToOutputOrEdgeConnector = GraphViewModule.inputToOutputOrEdgeConnector,
	private val reconnectOriginConnector: ReconnectOriginConnector = GraphViewModule.reconnectOriginConnector,
	private val reconnectDestinationConnector: ReconnectDestinationConnector = GraphViewModule.reconnectDestinationConnector,
	private val eventBus: EventBus = BaseModule.eventBus
) : DrawingImpl<T>(), GraphView<T> {

	constructor() : this(Translations.getString("graph.name.unknown"))
	constructor(name: String) : this(GraphModelModule.graphFactory.invoke(name))

	companion object {
		private val LOG by logger(GraphViewImpl::class)
	}

	/** Resets the current [Scenario] and [ScenarioStep] when the [Scheduler] is activated or deactivated. */
	private val schedulerActivationObserver: (SchedulerActivationStateEvent) -> Unit = {
		currentScenario = null
		currentScenarioStep = null
	}

	/** Manages the [NetView]s for all [Net]s of the [Graph].*/
	private val netViewMap: MutableMap<Net<Any>, NetView<Any>> = mutableMapOf()

	init {
		eventBus.register(SchedulerActivationStateEvent::class, schedulerActivationObserver)
	}

	/** ---- [Any] */

	override fun toString(): String {
		return graph?.name?.value ?: ""
	}

	/** ---- UI properties */

	var name: String
		get() = graph!!.name.value
		set(value) {
			graph!!.name.value = value
		}

	var translatableName: TranslatableText
		get() = graph!!.name.translation
		set(value) {
			graph!!.name.translation = value
		}

	var shortDescription: TextProperty
		get() = TextProperty(graph!!.description.value)
		set(value) {
			graph!!.description.value = value.text
		}

	var translatableShortDescription: TranslatableText
		get() = graph!!.description.translation
		set(value) {
			graph!!.description.translation = value
		}

	var propagationDelay: Long?
		get() = graph!!.propagationDelay
		set(value) {
			graph!!.propagationDelay = value
		}

	var script: TextProperty
		get() = TextProperty(graph!!.script)
		set(value) {
			graph!!.script = value.text
		}

	/** ---- [GraphView] interface */

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
				LOG.debug("Scenario '${value.name}' set in Graph '$name'")
			} else {
				LOG.debug("No current Scenario in Graph '$name'")
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
				LOG.debug("ScenarioStep '${value.name}' of Scenario '${currentScenario!!.name}' set in Graph '$name'")
			} else {
				LOG.debug("No current ScenarioStep in Graph '$name'")
			}
			eventBus.post(ScenarioStepEvent(this, oldValue, value))
		}


	override fun bind() {
		for (graphElementView in getDrawables()) {
			graphElementView.bind(graph!!)
		}
	}

	override fun checkDesign(): Boolean {
		val issues = getDrawables { it.model?.designError != null }.map {
			IssueImpl(
				severity = IssueSeverity.Error,
				name = Translations.getString("graph.designError.name"),
				description = it.model!!.designError?.description,
				origin = "${it.type} (${it.id})",
				context = null
			)
		}
		issues.forEach { eventBus.post(it) }
		return issues.isEmpty()
	}

	override fun cloneForExistingModel(model: Graph, storableCreator: StorableCreator): GraphView<T> {
		LOG.trace("clone '${model.name}'for existing model")
		val clone = storableCloner.clone(
			this,
			GlobalIdentityReflector(),
			storableCreator,
			ReferenceResolverProxy(
				GraphReferenceResolver(model),
				ReferenceResolverImpl()
			)
		) as GraphViewImpl<T>
		clone.graph = model
		return clone
	}

	override fun getEdgeViews(): ImmutableList<EdgeView<Any>> {
		return getDrawables { it is EdgeView<*> } as ImmutableList<EdgeView<Any>>
	}

	override fun getEdgeView(port: Port<*>): EdgeView<Any>? {
		return getEdgeViews().firstOrNull { it.originPort == port || it.destinationPort == port }
	}

	override fun getGraphPortViews(): ImmutableList<GraphPortView<GraphPort<Any>>> {
		return getDrawables { it is GraphPortView<*> } as ImmutableList<GraphPortView<GraphPort<Any>>>
	}

	override fun getControlViewSources(): ImmutableList<ControlViewSource<Vertice>> {
		return getDrawables { it is ControlViewSource<*> } as ImmutableList<ControlViewSource<Vertice>>
	}

	override fun getElementViews(element: GraphElement): ImmutableList<GraphElementView<*>> {
		return getDrawables { it.model == element }
	}

	override fun getGraphPortView(portName: String): GraphPortView<GraphPort<Any>>? {
		return getDrawable { it is GraphPortView<*> && it.model!!.name == portName } as GraphPortView<GraphPort<Any>>?
	}

	override fun getControlViewSource(controlId: String): ControlViewSource<Vertice>? {
		return getDrawable { it is ControlViewSource<*> && it.controlId == controlId } as ControlViewSource<Vertice>?
	}

	override fun getSubGraphVerticeViews(): ImmutableList<SubGraphVerticeView<SubGraphVertice>> {
		return getDrawables { it is SubGraphVerticeView<*> } as ImmutableList<SubGraphVerticeView<SubGraphVertice>>
	}

	/** ---- [Storable] interface */

	override fun getStorableChildren(): Iterator<Storable> {
		val list = mutableListOf<Storable>()
		list.add(scenarios)
		list.add(usecases)
		list.addAll(netViewMap.values)
		return ConcatIterator(super.getStorableChildren(), list.iterator())
	}

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
				resolveAfter = listOf(netView.storableId)
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
			// If NetViewElements are resolved BEFORE a NetView, a corresponding dummy NetView already
			// exists in netViewMap and must be replaced by the one just read and resolved, because that one
			// holds the essential properties.
			val resolvedNetView = reference.additionalInfo as NetView<Any>
			val existingNetView = netViewMap[resolvedNetView.net]
			existingNetView?.getElements()?.forEach {
				resolvedNetView.add(it)
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
		for (graphElementView in getDrawables()) {
			graphElementView.dispose()
		}
		scenarios.dispose()
		usecases.dispose()
		eventBus.unregister(SchedulerActivationStateEvent::class, schedulerActivationObserver)
	}

	// ---- [DrawableContainerImpl] */

	/**
	 * Returns first all [VerticeView]s and then all other [GraphElementView]s, so that [EdgeView]s don't
	 * overwrite [VerticeView] boundaries when being connected to them.
	 * By doing so, we accept the negative side affect that simple [Component]s such as [RectangleComponent]s
	 * can never be drawn above [VerticeView]s, but it's more important that [RectangleComponent]s can be used
	 * as subsystem boundaries, which are always drawn above all other [GraphElementView]s.
	 */
	override fun drawablesInDrawingOrder(): ImmutableList<T> {
		val drawables = mutableListOf<T>()
		drawables.addAll(super.drawablesInDrawingOrder().filter { it !is VerticeView<*> })
		drawables.addAll(super.drawablesInDrawingOrder().filter { it is VerticeView<*> })
		return drawables.toImmutableList()
	}

	override fun createInputEventHandler(): InputEventHandler<InputEventContext> {
		return GraphViewInputEventHandler() as InputEventHandler<InputEventContext>
	}

	/** Overridden in order to add the [GraphElement] to the [Graph] that this [GraphView] displays.*/
	override fun add(drawable: T, index: Int): DrawableContainer<T> {
		if (!contains(drawable)) {
			if (!readingFromStore) {
				if (drawable.model != null) {
					graph?.add(drawable.model!!)
				}
			}
			if (drawable is NetViewElement<*>) {
				addNetViewElement(drawable as NetViewElement<Any>)
			}
			return super.add(drawable, index)
		}
		return this
	}

	/** Overridden in order to remove the [GraphElement] from the [Graph] that this [GraphView] displays.*/
	override fun remove(drawable: T): DrawableContainer<T> {
		if (graph != null) {
			if (drawable.model != null && getElementViews(drawable.model!!).size == 1) {
				graph!!.remove(drawable.model!!)
			}
			if (drawable is NetViewElement<*>) {
				removeNetViewElement(drawable as NetViewElement<Any>)
			}
		}
		return super.remove(drawable)
	}

	override fun clear(): DrawableContainer<T> {
		super.clear()
		if (graph != null) {
			// graph should only be `null` during deserialization
			graph!!.clear()
		}
		return this
	}

	/** ---- [GraphViewImpl] */

	private fun addNetView(netView: NetView<Any>) {
		if (!netViewMap.containsKey(netView.net)) {
			netViewMap[netView.net] = netView
		}
	}

	private fun addNetViewElement(elem: NetViewElement<Any>) {
		var netView = netViewMap[elem.net]
		if (netView == null) {
			netView = NetViewImpl(elem.net!!)
			addNetView(netView)
		}
		netView.add(elem)
	}

	private fun removeNetViewElement(elem: NetViewElement<Any>) {
		val netView = netViewMap[(elem.net)]
		if (netView != null) {
			netView.remove(elem)
			if (netView.isEmpty) {
				netViewMap.remove(netView.net)
			}
		}
	}

	/**
	 * Intercepts [MouseEvent]s on [VerticeView] in order to forward them to the injected connectors.
	 *
	 * This relieves the [VerticeView] implementations from the burden to provide constructor injection parameters
	 * for all kinds of connectors, for the [EdgeViewFactory] and lots of other injected objects.
	 */
	private inner class GraphViewInputEventHandler : DrawableContainerInputEventHandler<T, EditInputEventContext>(this@GraphViewImpl) {

		private var target: InputEventHandler<EditInputEventContext>? = null

		override fun mouseMoved(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
			if (target != null) {
				target = target?.mouseMoved(context)
				if (target != null) {
					return target
				}
			}

			val drawable = getDrawableAt(context.x, context.y)
			if (drawable is VerticeView<*>) {
				val portView = drawable.getPortViewAtConnectionPoint(context.x, context.y)
				if (portView != null && portView.connectable) {
					if (portView.port.portType.isOutput) {
						if (portView.port.isConnected) {
							LOG.debug("delegating mouseMoved to ReconnectOriginConnector")
							reconnectOriginConnector.useFor(getEdgeView(portView.port)!!)
							target = reconnectOriginConnector
						} else {
							LOG.debug("delegating mouseMoved to OutputToInputConnector")
							target = outputToInputConnector
							outputToInputConnector.useFor(drawable)
						}
					} else if (portView.port.portType.isInput) {
						if (portView.port.isConnected) {
							LOG.debug("delegating mouseMoved to ReconnectDestinationConnector")
							reconnectDestinationConnector.useFor(getEdgeView(portView.port)!!)
							target = reconnectDestinationConnector
						} else {
							LOG.debug("delegating mouseMoved to InputToOutputOrEdgeConnector")
							target = inputToOutputOrEdgeConnector
							inputToOutputOrEdgeConnector.useFor(drawable)
						}
					}
					if (target != null) {
						target = target!!.mouseMoved(context)
					}
					if (target != null) {
						return target
					}
				}
			}

			return super.mouseMoved(context)
		}
	}
}