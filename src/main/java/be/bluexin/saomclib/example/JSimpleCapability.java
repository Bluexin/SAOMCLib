package be.bluexin.saomclib.example;

import be.bluexin.saomclib.LogHelper;
import be.bluexin.saomclib.SAOMCLib;
import be.bluexin.saomclib.capabilities.AbstractCapability;
import be.bluexin.saomclib.capabilities.AbstractEntityCapability;
import be.bluexin.saomclib.capabilities.Key;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Part of saomclib by Bluexin.
 *
 * @author Bluexin
 */
public class JSimpleCapability extends AbstractEntityCapability {
    @Key
    public static ResourceLocation KEY = new ResourceLocation(SAOMCLib.MODID, "simpleCapabilityJava");

    private int num = 0;

    @NotNull
    @Override
    public AbstractCapability setup(@NotNull Entity param) {
        super.setup(param);
        LogHelper.INSTANCE.logInfo("Set up for " + param.getClass());
        return this;
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("num", num);
        Entity ref = reference.get();
        LogHelper.INSTANCE.logInfo("Writing " + num + " on remote=" + (ref == null ? "null" : ref.worldObj.isRemote) + '.');
        compound.setTag(KEY.toString(), tag);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        num = compound.getInteger("num");
        Entity ref = reference.get();
        LogHelper.INSTANCE.logInfo("Reading " + num + " on remote=" + (ref == null ? "null" : ref.worldObj.isRemote) + '.');
    }
}
