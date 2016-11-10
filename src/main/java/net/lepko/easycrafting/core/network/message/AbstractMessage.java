package net.lepko.easycrafting.core.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;

public abstract class AbstractMessage {
    public abstract void write(ByteBuf target);
    public abstract void read(ByteBuf source);
    public abstract void run(EntityPlayer player, Side side);
}
