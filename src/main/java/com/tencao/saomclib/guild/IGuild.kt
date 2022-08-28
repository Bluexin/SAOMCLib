package com.tencao.saomclib.guild

import com.tencao.saomclib.party.IParty

abstract class IGuild : IParty() {

    abstract val name: String

    // TODO Add permission system
}
