package be.bluexin.saomclib.example

import be.bluexin.saomclib.LogHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.living.LivingAttackEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Part of saomclib by Bluexin.
 *
 * @author Bluexin
 */
class ExampleEventHandler {

    @SubscribeEvent
    fun attackEntity(e: LivingAttackEvent) {
        if (e.entity.world.isRemote) return

        if (e.source.entity is EntityPlayer) {
            LogHelper.logInfo("${e.entityLiving} attacked by ${e.source.entity}")
            val rand = (Math.random() * 1000).toInt()
            LogHelper.logInfo("Rand: $rand")
            e.source.entity!!.getCapability(SimpleCapability.CAP_INSTANCE, null).num = rand
        }
    }
}
