package be.bluexin.saomclib.guild

import be.bluexin.saomclib.party.IParty

abstract class IGuild: IParty() {

    abstract val name: String

    //TODO Add permission system
}