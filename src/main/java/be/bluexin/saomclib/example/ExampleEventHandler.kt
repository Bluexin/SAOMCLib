package be.bluexin.saomclib.example

import be.bluexin.saomclib.LogHelper
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
        if (!e.entity.worldObj.isRemote && e.source.entity is EntityPlayer) {
            LogHelper.logInfo("${e.entityLiving} attacked by ${e.source.entity}.")
            val rand: Int = (1000 * Math.random()).toInt()
            LogHelper.logInfo("Random: $rand.")
            (e.source.entity!!.getExtendedProperties(SimpleCapability.KEY.toString()) as SimpleCapability).num = rand
            (e.source.entity!!.getExtendedProperties(SimpleCapability.KEY.toString()) as SimpleCapability).sync()
        }
    }
}
