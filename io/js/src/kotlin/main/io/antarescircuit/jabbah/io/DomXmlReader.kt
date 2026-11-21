package io.antarescircuit.jabbah.io

import io.antarescircuit.jabbah.base.collection.Stack
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.asList
import org.w3c.dom.get
import org.w3c.dom.parsing.DOMParser

/**
 * An [XmlReader] for reading W3C XML DOM documents.
 */
class DomXmlReader(document: Document) : XmlReader {

    constructor(str: String): this(DOMParser().parseFromString(preserveAttributeWhitespace(str), "application/xml"))

    companion object {
        /**
         * XML parsers normalize tabs and line breaks in attribute values to spaces, but multi-line values such as
         * scripts are stored as plain attributes (the JVM's Electric XML keeps them). Hence these characters are
         * replaced by character references inside attribute values before parsing.
         */
        internal fun preserveAttributeWhitespace(xml: String): String {
            if (xml.none { it == '\n' || it == '\t' || it == '\r' }) return xml
            val sb = StringBuilder(xml.length + 64)
            var i = 0
            var inTag = false
            var quote: Char? = null
            while (i < xml.length) {
                val c = xml[i]
                if (quote != null) {
                    when (c) {
                        quote -> quote = null
                        '\n' -> { sb.append("&#10;"); i++; continue }
                        '\r' -> { sb.append("&#13;"); i++; continue }
                        '\t' -> { sb.append("&#9;"); i++; continue }
                    }
                } else if (inTag) {
                    if (c == '"' || c == '\'') quote = c else if (c == '>') inTag = false
                } else if (c == '<') {
                    // Copy comments, CDATA sections and processing instructions unchanged
                    val end = when {
                        xml.startsWith("<!--", i) -> xml.indexOf("-->", i).let { if (it < 0) -1 else it + 3 }
                        xml.startsWith("<![CDATA[", i) -> xml.indexOf("]]>", i).let { if (it < 0) -1 else it + 3 }
                        xml.startsWith("<?", i) -> xml.indexOf("?>", i).let { if (it < 0) -1 else it + 2 }
                        else -> null
                    }
                    if (end != null) {
                        val stop = if (end < 0) xml.length else end
                        sb.append(xml, i, stop)
                        i = stop
                        continue
                    }
                    inTag = true
                }
                sb.append(c)
                i++
            }
            return sb.toString()
        }
    }

    private val stack = Stack<Element>()

    init {
        stack.push(document.documentElement!!)
    }

    /** ---- [XmlReader] interface */

    override fun getName(): String {
        return stack.peek().tagName
    }

    override fun getAttributeValue(name: String): String {
        return stack.peek().getAttribute(name)!!
    }

    override fun hasAttribute(name: String): Boolean =
        stack.peek().getAttribute(name) != null

    override fun hasElement(name: String): Boolean =
        getChildElement(name) != null

    private fun getChildElement(name: String): Element? {
        val children = stack.peek().childNodes
        for (i in 0 until children.length) {
            if (children[i]!!.nodeName.equals(name, ignoreCase = true)) {
                return children[i] as Element?
            }
        }
        return null
    }

    override fun getElementNames(): Set<String> =
        stack.peek()
            .children
            .asList()
            .map { it.nodeName }
            .toSet()

    override fun getElementsCount(): Int {
        return stack.peek().childElementCount
    }

    override fun descend(name: String) {
        stack.push(getChildElement(name)!!)
    }

    override fun descend(index: Int) {
        stack.push(stack.peek().children[index - 1]!!)
    }

    override fun ascend() {
        stack.pop()
    }

    override fun getText(name: String): String = getChildElement(name)!!.textContent!!
}