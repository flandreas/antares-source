package io.antarescircuit.antares.ai

import io.antarescircuit.jabbah.edit.Look

/**
 * Assigns deterministic, left-to-right locations to components created by an assistant plan.
 *
 * Model-provided coordinates are only used to keep the requested vertical order. The actual
 * locations are derived from the connections, because the model does not know component sizes and
 * cannot reliably reserve enough room for components or wires.
 */
object AiPlanLayouter {

	/** Horizontal distance between columns of components.*/
	private const val COLUMN_SPACING = Look.SCALE * 12

	/** Minimum vertical distance between components in the same column.*/
	private const val ROW_SPACING = Look.SCALE * 12

	fun layout(plan: AiValidatedPlan): AiValidatedPlan {
		val operations = mutableListOf<AiOperation>()
		val segment = mutableListOf<AiOperation>()

		plan.operations.forEach { operation ->
			if (operation is AiOperation.ClearCircuit) {
				operations.addAll(layoutSegment(segment))
				segment.clear()
				operations.add(operation)
			} else {
				segment.add(operation)
			}
		}
		operations.addAll(layoutSegment(segment))

		return plan.copy(operations = operations)
	}

	private fun layoutSegment(operations: List<AiOperation>): List<AiOperation> {
		val components = operations.filterIsInstance<AiOperation.AddComponent>()
		if (components.isEmpty()) {
			return operations
		}

		val order = components.mapIndexed { index, component -> component.ref.id to index }.toMap()
		val predecessors = components.associate { it.ref.id to mutableSetOf<String>() }
		val successors = components.associate { it.ref.id to mutableSetOf<String>() }

		operations.filterIsInstance<AiOperation.Connect>().forEach { connection ->
			val from = (connection.from as? AiRef.New)?.id
			val to = (connection.to as? AiRef.New)?.id
			if (from != null && to != null && from in order && to in order) {
				predecessors.getValue(to).add(from)
				successors.getValue(from).add(to)
			}
		}

		val layers = assignLayers(components, predecessors, successors, order)
		val normalizedLayers = layers.values.distinct().sorted()
			.mapIndexed { index, layer -> layer to index }.toMap()
		val rows = mutableMapOf<String, Int>()
		val located = mutableMapOf<String, AiOperation.AddComponent>()

		components.groupBy { normalizedLayers.getValue(layers.getValue(it.ref.id)) }
			.entries.sortedBy { it.key }
			.forEach { (layer, column) ->
				val sorted = column.sortedWith(
					compareBy<AiOperation.AddComponent>(
						{ predecessorRow(it.ref.id, predecessors, rows) },
						{ it.y ?: Int.MAX_VALUE },
						{ order.getValue(it.ref.id) }))
				val rowSpacing = maxOf(
					ROW_SPACING,
					sorted.maxOf { (it.inputCount * 2 + 4) * Look.GRID })

				sorted.forEachIndexed { row, component ->
					rows[component.ref.id] = row
					located[component.ref.id] = component.copy(x = layer * COLUMN_SPACING, y = row * rowSpacing)
				}
			}

		return operations.map { operation ->
			if (operation is AiOperation.AddComponent) located.getValue(operation.ref.id) else operation
		}
	}

	private fun assignLayers(
		components: List<AiOperation.AddComponent>,
		predecessors: Map<String, Set<String>>,
		successors: Map<String, Set<String>>,
		order: Map<String, Int>
	): Map<String, Int> {
		val remainingPredecessors = predecessors.mapValues { it.value.size }.toMutableMap()
		val layers = mutableMapOf<String, Int>()
		val ready = components
			.filter { remainingPredecessors.getValue(it.ref.id) == 0 }
			.sortedBy { order.getValue(it.ref.id) }
			.mapTo(ArrayDeque()) { it.ref.id }

		while (ready.isNotEmpty()) {
			val id = ready.removeFirst()
			val layer = predecessors.getValue(id).maxOfOrNull { layers.getValue(it) + 1 } ?: 0
			layers[id] = layer

			successors.getValue(id).sortedBy { order.getValue(it) }.forEach { successor ->
				val remaining = remainingPredecessors.getValue(successor) - 1
				remainingPredecessors[successor] = remaining
				if (remaining == 0) {
					ready.addLast(successor)
				}
			}
		}

		// Components in feedback loops have no topological order, and unconnected components no
		// meaningful one. Both get the column suggested by their role, which keeps them deterministic.
		components.forEach { component ->
			val id = component.ref.id
			if (id !in layers || (predecessors.getValue(id).isEmpty() && successors.getValue(id).isEmpty())) {
				layers[id] = defaultLayer(component.type)
			}
		}

		alignLayers(components, layers, ::isInputComponent, chooseLayer = { it.minOrNull() })
		alignLayers(components, layers, ::isOutputComponent, chooseLayer = { it.maxOrNull() })

		return layers
	}

	private fun alignLayers(
		components: List<AiOperation.AddComponent>,
		layers: MutableMap<String, Int>,
		matches: (AiComponentType) -> Boolean,
		chooseLayer: (List<Int>) -> Int?,
	) {
		val matching = components.filter { matches(it.type) }
		val layer = chooseLayer(matching.map { layers.getValue(it.ref.id) }) ?: return
		matching.forEach { layers[it.ref.id] = layer }
	}

	private fun isInputComponent(type: AiComponentType): Boolean =
		type == AiComponentType.Input || type == AiComponentType.Switch

	private fun isOutputComponent(type: AiComponentType): Boolean =
		type == AiComponentType.Output || type == AiComponentType.Led

	private fun defaultLayer(type: AiComponentType): Int = when (type) {
		AiComponentType.Input, AiComponentType.Switch, AiComponentType.Constant, AiComponentType.Clock -> 0
		AiComponentType.Output, AiComponentType.Led -> 2
		AiComponentType.Subcircuit, AiComponentType.Splitter, AiComponentType.Concentrator,
		AiComponentType.TriStateBuffer -> 1
		else -> 1
	}

	private fun predecessorRow(
		id: String,
		predecessors: Map<String, Set<String>>,
		rows: Map<String, Int>
	): Double {
		val predecessorRows = predecessors.getValue(id).mapNotNull { rows[it] }
		return if (predecessorRows.isEmpty()) Double.MAX_VALUE else predecessorRows.average()
	}
}
