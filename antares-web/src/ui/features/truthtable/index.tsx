// Truth tables, boolean expressions, circuit analysis and synthesis.
import { ScanSearch, Sigma, Table2 } from 'lucide-react'
import { documentEditors, documentInspectors, newItemActions, projectMenuActions, register } from '../registry'
import { analyseCircuit, newExpression, newTruthTable } from './dialogs'
import { ExpressionEditor } from './ExpressionEditor'
import { ExpressionInspector, TruthTableInspector } from './Inspectors'
import { TruthTableEditor } from './TruthTableEditor'
import './truthtable.css'

register(newItemActions, (a) => a.label, 
	{ label: 'Truth table…', icon: <Table2 size={15} />, run: newTruthTable },
	{ label: 'Boolean expressions…', icon: <Sigma size={15} />, run: newExpression },
)
register(projectMenuActions, (a) => a.label, {
	label: 'Analyse circuit…',
	icon: <ScanSearch size={15} />,
	enabled: (s) => !!s.circuit && s.circuit.type === 'Digital' && !s.document,
	run: analyseCircuit,
})
documentEditors.truthTable = TruthTableEditor
documentInspectors.truthTable = TruthTableInspector
documentEditors.expression = ExpressionEditor
documentInspectors.expression = ExpressionInspector
