package be.bluexin.saomclib.guild

import be.bluexin.saomclib.party.IParty

interface IGuild: IParty {

    val name: String

    //TODO Add permission system
}