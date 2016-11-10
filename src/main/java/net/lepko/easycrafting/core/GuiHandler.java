package net.lepko.easycrafting.core;

import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.lepko.easycrafting.EasyCrafting;
import net.lepko.easycrafting.core.inventory.gui.IGuiTile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public enum GuiHandler implements IGuiHandler {
    INSTANCE;

    public static void openGui(EntityPlayer player, World world, BlockPos pos) {
        FMLNetworkHandler.openGui(player, EasyCrafting.INSTANCE, 0, 
        		world, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public Object getServerGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (te instanceof IGuiTile) {
            return ((IGuiTile) te).getServerGuiElement(player, te);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int guiID, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (te instanceof IGuiTile) {
            return ((IGuiTile) te).getClientGuiElement(player, te);
        }
        return null;
    }
}