package net.lepko.easycrafting.core.block;

import net.lepko.easycrafting.Ref;
import net.lepko.easycrafting.core.GuiHandler;
import net.lepko.easycrafting.core.inventory.gui.IGuiTile;
import net.lepko.easycrafting.core.recipe.RecipeManager;
import net.lepko.easycrafting.core.util.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

import javax.annotation.Nullable;

public class BlockTable extends BlockContainer {

    public static String[] names = { "easy_crafting", "auto_crafting" };

    public BlockTable() {
        super(Material.WOOD);
        setHardness(2.5F);
        setUnlocalizedName("table");
        setCreativeTab(CreativeTabs.DECORATIONS);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int meta = 0; meta < names.length; meta++) {
            list.add(new ItemStack(item, 1, meta));
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(pos);

        if (te == null || player.isSneaking()) {
            return false;
        }

        if (te instanceof TileEntityEasyCrafting) {
            RecipeManager.scanRecipes();
        }

        if (te instanceof IGuiTile) {
            GuiHandler.openGui(player, world, pos);
            return true;
        }

        return false;
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityAutoCrafting) {
            InventoryUtils.dropItems(world.getTileEntity(pos), ((TileEntityAutoCrafting) te).SLOTS);
        } else {
            InventoryUtils.dropItems(world.getTileEntity(pos));
        }
        super.onBlockHarvested(world, pos, state, player);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        switch (meta) {
            case 1:
                return new TileEntityAutoCrafting();
            default:
                return new TileEntityEasyCrafting();
        }
    }
}