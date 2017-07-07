package ch.scorpion.jabbah.base.swing

import javax.swing.JPanel

class StateBar : JPanel() {

    enum class NetState {
        NET_ACTIVE, NET_INACTIVE
    }

    var netState: NetState = NetState.NET_INACTIVE
}