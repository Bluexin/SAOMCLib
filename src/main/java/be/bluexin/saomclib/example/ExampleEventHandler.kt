package be.bluexin.saomclib.example

import be.bluexin.saomclib.SAOMCLib
import be.bluexin.saomclib.world
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.living.LivingAttackEvent

/**
 * Part of saomclib by Bluexin.
 *
 * To register the capabilities or this handler, you can use :
 *   MinecraftForge.EVENT_BUS.register(ExampleEventHandler())
 *   PacketPipeline.registerMessage(SyncEntityCapabilityPacket::class.java, SyncEntityCapabilityPacket.Companion.Handler::class.java)
 *   CapabilitiesHandler.registerEntityCapability(SimpleCapability::class.java, SimpleCapability.Companion.Storage(), { it is EntityPlayer })
 *   CapabilitiesHandler.registerEntityCapability(JSimpleCapability::class.java, JSimpleCapability.Storage(), { it is EntityPlayer })
 *
 * Or the java equivalents :
 *   MinecraftForge.EVENT_BUS.register(new ExampleEventHandler())
 *   PacketPipeline.INSTANCE.registerMessage(SyncEntityCapabilityPacket.class, SyncEntityCapabilityPacket.Companion.Handler.class)
 *   CapabilitiesHandler.INSTANCE.registerEntityCapability(SimpleCapability.class, new SimpleCapability.Companion.Storage(), entity -> entity instanceof EntityPlayer)
 *   CapabilitiesHandler.INSTANCE.registerEntityCapability(JSimpleCapability.class, new JSimpleCapability.Storage(), entity -> entity instanceof EntityPlayer)
 *
 * @author Bluexin
 */
class ExampleEventHandler {

    @SubscribeEvent
    fun attackEntity(e: LivingAttackEvent) {
        if (!e.entity.world.isRemote && e.source.sourceOfDamage is EntityPlayer) {
            SAOMCLib.LOGGER.info("${e.entityLiving} attacked by ${e.source.sourceOfDamage}.")
            val rand: Int = (1000 * Math.random()).toInt()
            SAOMCLib.LOGGER.info("Random: $rand.")
            val cap = e.source.sourceOfDamage!!.getCapability(SimpleCapability.CAP_INSTANCE, null)?:return
            cap.num = rand
            cap.sync()
        }
    }
}
