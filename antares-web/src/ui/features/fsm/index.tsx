// Finite state machines: "New state machine…", the FSM editor and its inspector.
import { Workflow } from 'lucide-react'
import { documentEditors, documentInspectors, newItemActions, register } from '../registry'
import { newStateMachine } from './actions'
import { FsmEditor } from './FsmEditor'
import { FsmInspector } from './FsmInspector'
import './fsm.css'

register(newItemActions, (a) => a.label, { label: 'State machine…', icon: <Workflow size={15} />, run: newStateMachine })
documentEditors.fsm = FsmEditor
documentInspectors.fsm = FsmInspector
