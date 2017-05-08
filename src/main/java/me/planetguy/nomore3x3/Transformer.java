package me.planetguy.nomore3x3;

import java.io.IOException;
import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

import me.planetguy.lib.util.Debug;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.launchwrapper.IClassTransformer;


public class Transformer extends SimplePatcher {

	@Override
	public byte[] transform(String name, String transformedName, byte[] classIn) {
		if(transformedName.equals("net.minecraft.client.gui.inventory.GuiCrafting"))
			return applyPatchClass(classIn, "me.planetguy.nomore3x3.GuiLazyCrafting");
		return classIn;
	}
}
