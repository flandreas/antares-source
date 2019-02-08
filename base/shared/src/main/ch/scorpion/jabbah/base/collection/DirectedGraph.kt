package ch.scorpion.jabbah.base.collection

import ch.scorpion.jabbah.base.exception.NoSuchElementException

/**
 * http://keithschwarz.com/interesting/code/?dir=topological-sort.
 * @param <T>
 */
class DirectedGraph<T> : Iterable<T> {

    /**
     * A map from nodes in the graph to sets of outgoing edges. Each set of edges is represented by a map
     * from edges to doubles.
     */
    private val mGraph = mutableMapOf<T, MutableSet<T>>()

    /** ---- [Iterable] interface */

    override fun iterator(): Iterator<T> {
        return mGraph.keys.iterator()
    }

    /** ---- [DirectedGraph] */

    /**
     * Adds a new node to the graph. If the node already exists, this function is a no-op.
     *
     * @param node The node to add.
     * @return Whether or not the node was added.
     */
    fun addNode(node: T): Boolean {
        // If the node already exists, don't do anything.
        if (mGraph.containsKey(node))
            return false

        // Otherwise, add the node with an empty set of outgoing edges.
	    mGraph[node] = mutableSetOf()
        return true
    }

    /**
     * Given a start node, and a destination, adds an arc from the start node to the destination. If an arc already
     * exists, this operation is a no-op. If either endpoint does not exist in the graph, throws a
     * NoSuchElementException.
     *
     * @param start The start node.
     * @param dest The destination node.
     * @throws NoSuchElementException If either the start or destination nodes do not exist.
     */
    fun addEdge(start: T, dest: T) {
        // Confirm both endpoints exist.
        if (!mGraph.containsKey(start) || !mGraph.containsKey(dest))
            throw NoSuchElementException("Both nodes must be in the graph.")

        // Add the edge
        mGraph[start]?.add(dest)
    }

    /**
     * Removes the edge from start to dest from the graph. If the edge does not exist, this operation is a no-op. If
     * either endpoint does not exist, this throws a NoSuchElementException.
     *
     * @param start The start node.
     * @param dest The destination node.
     * @throws NoSuchElementException If either node is not in the graph.
     */
    fun removeEdge(start: T, dest: T) {
        // Confirm both endpoints exist.
        if (!mGraph.containsKey(start) || !mGraph.containsKey(dest))
            throw NoSuchElementException("Both nodes must be in the graph.")

        mGraph[start]?.remove(dest)
    }

    /**
     * Given two nodes in the graph, returns whether there is an edge from the first node to the second node. If either
     * node does not exist in the graph, throws a NoSuchElementException.
     *
     * @param start The start node.
     * @param end The destination node.
     * @return Whether there is an edge from start to end.
     * @throws NoSuchElementException If either endpoint does not exist.
     */
    fun edgeExists(start: T, end: T): Boolean {
        // Confirm both endpoints exist.
        if (!mGraph.containsKey(start) || !mGraph.containsKey(end))
            throw NoSuchElementException("Both nodes must be in the graph.")

        return mGraph[start]!!.contains(end)
    }

    /**
     * Given a node in the graph, returns an immutable view of the edges leaving that node as a set of endpoints.
     *
     * @param node The node whose edges should be queried.
     * @return An immutable view of the edges leaving that node.
     * @throws NoSuchElementException If the node does not exist.
     */
    fun edgesFrom(node: T): Set<T> {
        /* Check that the node exists. */
        val arcs = mGraph[node] ?: throw NoSuchElementException("Source node does not exist.")

        return ImmutableSet(arcs)
    }

    /** Returns the number of nodes in the graph. */
    fun size(): Int {
        return mGraph.size
    }

    /** Returns whether the graph is empty. */
    fun isEmpty(): Boolean {
        return mGraph.isEmpty()
    }
}