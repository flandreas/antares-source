package ch.scorpion.jabbah.base

/**
 * Defines the interface of a visitor that can visit any hierarchical structures according to the visitor design
 * pattern.
 *
 * Objects that make up the hierarchical structure to be visited should offer a method that accepts a [HierarchyVisitor],
 * like `boolean accept(HierarchyVisitor visitor)`. In addition to the traditional visitor pattern, where the
 * visitor only has a `visit()` method, this hierarchical visitor pattern has two additional methods that allow the
 * visitor to distinguishing between siblings and children of composite nodes.
 *
 * The `accept` method of a composite node should be implemented as follows:
 *
 *      fun accept(HierarchyVisitor v): Boolean {
 *          if (v.visitEnter(this)) {
 *              Iterator<Child> iter = children.iterator();
 *              while (iter.hasNext() {
 *                  if (!iter.next().accept(v) {
 *                      break;
 *                  }
 *              }
 *          }
 *          return v.visitLeave(this);
 *      }
 *
 * The `accept` method of a leaf node should be implemented as follows:
 *
 *      fun accept(HierarchyVisitor v): Boolean {
 *          return v.visit(this);
 *      }
 *
 * See http://c2.com/cgi/wiki?HierarchicalVisitorPattern.
 */
interface HierarchyVisitor {

    /**
     * Called by a visited composite node before any children are visited.
     * @param node the visited composite node.
     * @return `false` if traversal has to be stopped.
     */
    fun visitEnter(node: Any): Boolean

    /**
     * Called by a visited node (either composite or leaf) in its accept method.
     * @param node the visited node.
     * @return `false` if traversal has to be stopped.
     */
    fun visit(node: Any): Boolean

    /**
     * Called by a visited composite node after the children have been visited.
     * @param node the visited composite node.
     * @return `false` if traversal has to be stopped.
     */
    fun visitLeave(node: Any): Boolean
}

open class EmptyHierarchyVisitor : HierarchyVisitor {

    override fun visitEnter(node: Any): Boolean = true

    override fun visit(node: Any): Boolean = true

    override fun visitLeave(node: Any): Boolean = true
}
