package com.tencao.saomclib.packets.party

enum class PartyType {
    MAIN,
    INVITE;
}

enum class Type {
    JOIN,
    INVITE,
    ACCEPTINVITE,
    CANCELINVITE,
    LEAVE,
    KICK,
    DISBAND,
    LEADERCHANGE,
    REFRESH;
}
