package be.bluexin.saomclib.mixin;

import be.bluexin.saomclib.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class ExampleMixin {

    @Inject(at = @At("HEAD"), method = "init()V")
    private void init(CallbackInfo info) {
        Constants.INSTANCE.getLOG().info("This line is printed by an example mod mixin from Fabric!");
        Constants.INSTANCE.getLOG().info("MC Version: {}", Minecraft.getInstance().getVersionType());
        Constants.INSTANCE.getLOG().info("Classloader: {}", this.getClass().getClassLoader());
    }
}