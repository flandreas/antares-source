package ch.scorpion.jabbah.graph.view

import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.*
import ch.scorpion.jabbah.io.*

/**
 * Abstract base implementation of the [GraphElementView] interface.
 * @param T the type of the model [GraphElement]
 */
abstract class AbstractGraphElementView<T : GraphElement>(
	styleProvider: StyleProvider,
	styleType: StyleType,
	model: T
) : AbstractComponent(styleProvider, styleType), GraphElementView<T> {

	companion object {
		const val STORABLE_MODEL_ID = "modelId"
	}

	/** Listens for changes of the model [GraphElement] and updates this view accordingly.*/
	private val modelListener = ModelListener()

	/** Determines whether this [AbstractGraphElementView] is currently resolving persistent references.*/
	protected var isResolving: Boolean = false
		private set

	/** ---- [GraphElementView] interface */

	override var model: T = model
		set(value) {
			val oldModel = field
			field = value
			modelExchanged(oldModel)
		}

	init {
		model.addGraphElementListener(modelListener)
	}

	override fun dispose() {
		model.removeGraphElementListener(modelListener)
	}

	/** ---- UI related properties */

	val modelId: Int
		get() = model.id

	var propagationDelay: Long
		get() = model.propagationDelay
		set(value) {
			model.propagationDelay = value
		}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		writer.writeInt(STORABLE_MODEL_ID, writer.provideIdentity(model))
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		readModelId(reader)
	}

	protected fun readModelId(reader: StoreReader) {
		if (reader.hasAttribute(STORABLE_MODEL_ID)) {
			val modelId = reader.readInt(STORABLE_MODEL_ID)
			if (modelId >= 0) {
				// There are Storables like ControlView wrapped in ControlViewComponent that don't have
				// a model at design time. The are linked to there Model when execution is started.
				reader.requestResolution(this, Reference(
					name = STORABLE_MODEL_ID,
					referenceId = modelId,
					resolveAfter = listOf(modelId)
				))
			}
		}
	}

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
		isResolving = true
		super.resolve(reference, referenceResolver)
		if (STORABLE_MODEL_ID == reference.name) {
			val storable: Storable? = referenceResolver.getStorable(reference.referenceId)
			if (storable is GraphElement) {
				model = storable as T
			}
		}
		isResolving = false
	}

	override fun bind(graph: Graph) {
		// empty
	}

	/** ---- [Component] interface */

	override val fixStyleType: Boolean get() = true

	/** ---- [AbstractGraphElementView] */

	/**
	 * Called by this class whenever the underlying model [GraphElement] has been exchanged, for example during
	 * deserialising from persistent store (which must use the default constructor).
	 * @param oldModel the former [GraphElement] model instance.
	 */
	protected open fun modelExchanged(oldModel: T?) {
		oldModel?.removeGraphElementListener(modelListener)
		model.addGraphElementListener(modelListener)
	}

	protected open fun handleStateChanged(event: GraphElementEvent) {
		invalidate()
		// TODO This leads to a repainting of the entire GraphView.
		// It should be sufficient to repaint the GraphView after a complete execution cycle
		validate()
	}

	protected open fun handleExecutionStarted(signalHandler: SignalHandler) {
		// empty
	}

	protected open fun handleExecutionStopped(signalHandler: SignalHandler) {
		// empty
	}

	private inner class ModelListener : GraphElementAdapter() {
		override fun stateChanged(e: GraphElementEvent) {
			handleStateChanged(e)
		}

		override fun executionStarted(signalHandler: SignalHandler) {
			handleExecutionStarted(signalHandler)
		}

		override fun executionStopped(signalHandler: SignalHandler) {
			handleExecutionStopped(signalHandler)
		}
	}
}