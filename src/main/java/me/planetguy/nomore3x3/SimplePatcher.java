package me.planetguy.nomore3x3;

import java.io.IOException;
import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;

public abstract class SimplePatcher implements IClassTransformer {
	
	//Patches the input class with all methods, fields, interfaces that are in
	//the class named by patchClass
	public byte[] applyPatchClass(byte[] inputClass, String patchClass){
		try {
			ClassNode classNode = new ClassNode();
			ClassReader oldClass = new ClassReader(inputClass);
			oldClass.accept(classNode, 0);
			
			ClassNode newClassNode = new ClassNode();
			
			ClassReader newClass = new ClassReader(patchClass);
			newClass.accept(newClassNode, 0);
			
			copyMethods(classNode, newClassNode);
			copyVariables(classNode, newClassNode);
			
			copyInterfaces(classNode, newClassNode);

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
	
	//Works on inputClass.
	//Any calls to owner.method() get redirected to newOwner.method() of the same name
	public byte[] redirectCalls(byte[] inputClass, String owner, String method, String newOwner){
		
		owner=owner.replace('.', '/');
		newOwner=newOwner.replace('.', '/');
		
		ClassNode classNode = new ClassNode();
		ClassReader oldClass = new ClassReader(inputClass);
		oldClass.accept(classNode, 0);
		
		for(MethodNode methodNode:classNode.methods){
			AbstractInsnNode insn=methodNode.instructions.getFirst();
			while(insn.getNext() != null) {
				if(insn instanceof MethodInsnNode){
					MethodInsnNode a=(MethodInsnNode) insn;
					if(a.owner.equals(owner) && a.name.equals(method)) {
						a.owner = newOwner;
					}
				}
				insn=insn.getNext();
			}
		}
		
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

		classNode.accept(writer);
		return writer.toByteArray();
	}
	
	private void copyInterfaces(ClassNode classNode, ClassNode newClassNode) {
		for(String i:newClassNode.interfaces){
			classNode.interfaces.add(i);
		}
	}


	//Copies methods from the old version 
	private void copyMethods(ClassNode classNode, ClassNode newClassNode){
		for(MethodNode node:newClassNode.methods){
			
			//Delete any old version of the method
			for(Iterator i=classNode.methods.iterator(); i.hasNext();) {
				MethodNode oldNode = (MethodNode) i.next();
				if(oldNode.name.equals(node.name))
					i.remove();
			}

			//Add the new version
			classNode.methods.add(node);

			AbstractInsnNode insn=node.instructions.getFirst();
			while(insn.getNext() != null){
				if(insn instanceof FieldInsnNode) {
					FieldInsnNode n=(FieldInsnNode) insn;
					if(n.owner.equals(newClassNode.name))
						n.owner=classNode.name;
				}

				if(insn instanceof MethodInsnNode){
					MethodInsnNode n=(MethodInsnNode)insn;
					if(n.owner.equals(newClassNode.name))
						n.owner=classNode.name;
				}

				insn=insn.getNext();
			}
		}
	}
	
	//Copies methods from the old version 
	private void copyVariables(ClassNode classNode, ClassNode newClassNode){
		for(FieldNode node:newClassNode.fields){
			System.out.println(node.name);
			classNode.fields.add(node);
		}
	}

}
