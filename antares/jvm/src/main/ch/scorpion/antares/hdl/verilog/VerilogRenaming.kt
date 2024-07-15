package ch.scorpion.antares.hdl.verilog

import ch.scorpion.antares.hdl.HDLRenaming
import ch.scorpion.jabbah.base.richtext.RichTextTokenType

class VerilogRenaming : HDLRenaming {

    companion object {
        private  val KEYWORDS = setOf(
            "always", "and", "assign", "automatic", "begin", "buf", "bufif0", "bufif1", "case", "casex",
            "casez", "cell", "cmos", "config", "deassign", "default", "defparam", "design",
            "disable", "edge", "else", "end", "endcase", "endconfig", "endfunction", "endgenerate",
            "endmodule", "endprimitive", "endspecify", "endtable", "endtask", "event", "for",
            "force", "forever", "fork", "function", "generate", "genvar", "highz0", "highz1",
            "if", "ifnone", "incdir", "include", "initial", "inout", "input", "instance", "integer",
            "join", "large", "liblist", "library", "localparam", "macromodule", "medium", "module",
            "nand", "negedge", "nmos", "nor", "noshowcancelledno", "not", "notif0", "notif1",
            "or", "output", "parameter", "pmos", "posedge", "primitive", "pull0", "pull1",
            "pulldown", "pullup", "pulsestyle_oneventglitch", "pulsestyle_ondetectglitch",
            "remos", "real", "realtime", "reg", "release", "repeat", "rnmos", "rpmos", "rtran",
            "rtranif0", "rtranif1", "scalared", "showcancelled", "signed", "small", "specify",
            "specparam", "strong0", "strong1", "supply0", "supply1", "table", "task", "time",
            "tran", "tranif0", "tranif1", "tri", "tri0", "tri1", "triand", "trior", "trireg",
            "unsigned", "use", "vectored", "wait", "wand", "weak0", "weak1", "while", "wire",
            "wor", "xnor", "xor")
    }

    override fun checkName(name: String): String {
        return if (isKeyword(name) || !isFirstCharValid(name)) {
            // Escaped identifier. The space is part of the identifier.
            return "\\$name "
        } else {
            cleanName(name)
        }
    }

    private fun cleanName(name: String): String {
        val sb = StringBuilder()
        var needEscaping = false
        for (c in name) {
            if (c in 'a'..'z' || c in 'A'..'Z' || c == '_' || c == '$') {
                sb.append(c)
            } else {
                when (c) {
                    '\\' -> {}
                    RichTextTokenType.OVERLINE.id[0], '~', '\u00AC' -> sb.append("not")
                    else -> {
                        sb.append(c)
                        needEscaping = true
                    }
                }
            }
        }

        if (needEscaping) {
            sb.insert(0, "\\")
            sb.append(" ")
        }

        return sb.toString()
    }

    private fun isKeyword(name: String): Boolean = KEYWORDS.contains(name.lowercase())

    private fun isFirstCharValid(name: String): Boolean {
        val c = name.first()
        return c in 'a'..'z' || c in 'A'..'Z' || c == '_'
    }
}