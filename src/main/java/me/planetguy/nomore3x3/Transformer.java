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
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.launchwrapper.IClassTransformer;


public class Transformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] classIn) {
		switch(transformedName){
		case "net.minecraft.client.gui.inventory.GuiCrafting":
			return transformGuiCrafting(classIn);
		default:
			return classIn;
		}
	}


	private byte[] transformGuiCrafting(byte[] bytes){
		try {
			ClassNode classNode = new ClassNode();
			ClassReader oldClass = new ClassReader(bytes);
			oldClass.accept(classNode, 0);
			
			ClassNode newClassNode = new ClassNode();
			
			ClassReader newClass = new ClassReader("me.planetguy.nomore3x3.GuiLazyCrafting");
			newClass.accept(newClassNode, 0);
			
			ParameterNode guiCrafting=new ParameterNode("net.minecraft.client.gui.inventory.GuiCrafting", 0);
			
			for(MethodNode node:newClassNode.methods){
				System.out.println(node.name);
				if(!node.name.equals("<init>")){
					
					//Delete the old version of the method
					for(Iterator i=classNode.methods.iterator(); i.hasNext();) {
						MethodNode oldNode = (MethodNode) i.next();
						if(oldNode.name.equals(node.name))
							i.remove();
					}
					
					//Add the new version
					classNode.methods.add(node);
					
					AbstractInsnNode instruction=node.instructions.getFirst();
					while(instruction.getNext() != null){
						if(instruction instanceof FieldInsnNode) {
							FieldInsnNode n=(FieldInsnNode) instruction;
							n.owner="net/minecraft/client/gui/inventory/GuiCrafting";
						}
						
						instruction=instruction.getNext();
					}
				}
			}

			//ASM specific for cleaning up and returning the final bytes for JVM processing.
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(writer);
			return writer.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
