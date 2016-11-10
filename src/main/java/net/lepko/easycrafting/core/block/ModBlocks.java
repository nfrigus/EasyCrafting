package net.lepko.easycrafting.core.block;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.lepko.easycrafting.Ref;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

@GameRegistry.ObjectHolder(Ref.MOD_ID)
public class ModBlocks {

    public static final Block table = new BlockTable();

    public static void setupBlocks() {
        GameRegistry.register(table, new ResourceLocation(Ref.MOD_ID,"table"));
        GameRegistry.register(new ItemBlockTable(table));
        //GameRegistry.registerCustomItemStack("easyCraftingTable", new ItemStack(table, 1, 0));
        //GameRegistry.registerCustomItemStack("autoCraftingTable", new ItemStack(table, 1, 1));

        GameRegistry.registerTileEntity(TileEntityEasyCrafting.class, "EasyCraftingTableTE");
        GameRegistry.registerTileEntity(TileEntityAutoCrafting.class, "AutoCraftingTableTE");
    }

    public static void setupRecipes() {
        GameRegistry.addShapelessRecipe(get("easyCraftingTable"), Blocks.CRAFTING_TABLE, Items.REDSTONE, Items.BOOK);
        GameRegistry.addShapedRecipe(get("autoCraftingTable"), "rsr", "scs", "rsr", 'r', Items.REDSTONE, 's', Blocks.STONE, 'c', Blocks.CRAFTING_TABLE);
    }

    private static ItemStack get(String name) {
    	return new ItemStack(table, 1, 1);
        //return GameRegistry.findItemStack(Ref.MOD_ID, name, 1);
    }
}
