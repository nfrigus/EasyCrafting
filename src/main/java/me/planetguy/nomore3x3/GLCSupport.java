package me.planetguy.nomore3x3;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import me.planetguy.lib.util.Debug;
import net.lepko.easycrafting.core.network.PacketHandler;
import net.lepko.easycrafting.core.network.message.MessageEasyCrafting;
import net.lepko.easycrafting.core.recipe.WrappedRecipe;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class GLCSupport {
	
	private static Method addSlot;
	
	static{
		Method[] methods=Container.class.getMethods();
		for(Method a:methods){
			Class[] args=a.getParameterTypes();
			if(args.length==1 && args[0].equals(Slot.class)){
				Debug.dbg("Found addSlotToContainer? "+a);
				addSlot=a;
			}
		}
	}

	public static void addSlotToContainer(Container container, Slot slot){
		try {
			addSlot.invoke(container, slot);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void craftThing(WrappedRecipe recipe, boolean isRightClick, boolean isShiftClick){
		PacketHandler.INSTANCE.sendToServer(new MessageEasyCrafting(recipe, isRightClick, isShiftClick));

	}

}
