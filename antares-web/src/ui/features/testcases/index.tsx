// Test cases, scenarios and use cases of the open circuit, shown as inspector tabs.
import { circuitPanels, register } from '../registry'
import { ScenariosPanel } from './ScenariosPanel'
import { TestsPanel } from './TestsPanel'
import './testcases.css'

register(circuitPanels, (p) => p.id, { id: 'tests', label: 'Tests', component: TestsPanel }, { id: 'scenarios', label: 'Scenarios', component: ScenariosPanel })
