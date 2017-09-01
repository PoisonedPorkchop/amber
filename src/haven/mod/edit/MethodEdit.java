package haven.mod.edit;

import javassist.*;
import javassist.bytecode.*;

import java.io.IOException;
import java.util.ArrayList;

public class MethodEdit {

    public static void editMethodAddEvent(CtClass target, MethodInfo method, CtClass eventClass, int start, int[] bytes, int stacksize, String constructorParameters) throws BadBytecode, NotFoundException, CannotCompileException {
        target.defrost();
        CodeAttribute codeAttribute = method.getCodeAttribute();
        CodeIterator iterator = codeAttribute.iterator();
        int classID = method.getConstPool().addClassInfo(eventClass);
        int constrnatID = method.getConstPool().addNameAndTypeInfo("<init>",constructorParameters);
        int constructID = method.getConstPool().addMethodrefInfo(classID,constrnatID);
        int callnatID = method.getConstPool().addNameAndTypeInfo("call","()V");
        int callID = method.getConstPool().addMethodrefInfo(classID,callnatID);
        iterator.insertGap(start,bytes.length);

        for (int i = 0; i < bytes.length; i++) {
            int byteCode = bytes[i];
            if (byteCode >= 0) {
                iterator.writeByte(byteCode, start+i);
            } else if (byteCode == -1) {
                iterator.writeByte(classID,start+i);
            } else if (byteCode == -2) {
                iterator.writeByte(constructID, start+i);
            } else if (byteCode == -3) {
                iterator.writeByte(callID, start+i);
            }
        }

        if(stacksize > codeAttribute.getMaxStack())
            codeAttribute.setMaxStack(stacksize);
    }

    public static void debugMethod(MethodInfo method)
    {
        CodeAttribute codeAttribute = method.getCodeAttribute();
        CodeIterator iterator = codeAttribute.iterator();
        System.out.println("Types: ");
        ArrayList<Integer> indices = new ArrayList<>();

        method.getConstPool().print();
        System.out.println("\nStackSize: " + codeAttribute.getMaxStack());
        System.out.println("\nLocals: " + codeAttribute.getMaxLocals() + "\n");

        while (iterator.hasNext()) {
            int index = 0;
            try {
                index = iterator.next();
            } catch (BadBytecode badBytecode) {
                badBytecode.printStackTrace();
            }
            indices.add(index);
        }

        for(int currentIndex = 0; indices.size() > currentIndex; currentIndex++)
        {
            if(currentIndex + 1 < indices.size())
            {
                int commandtype = indices.get(currentIndex);
                int dataindiceslength = (indices.get(currentIndex + 1) - commandtype);
                int[] data = new int[dataindiceslength-1];
                for(int start = commandtype + 1; start - commandtype < dataindiceslength; start++)
                {
                    data[(start - commandtype)-1] = iterator.byteAt(start);
                }
                System.out.println("Command at index " + commandtype + ": " + Mnemonic.OPCODE[iterator.byteAt(commandtype)] + "   * " + iterator.byteAt(commandtype));
                for(int dat : data)
                {
                    System.out.println("  * " + dat);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                int commandtype = indices.get(currentIndex);
                int dataindiceslength = ((iterator.getCodeLength()-1) - commandtype);
                int[] data = new int[dataindiceslength];
                for(int start = commandtype + 1; start - commandtype < dataindiceslength; start++)
                {
                    data[(start - commandtype)-1] = iterator.byteAt(start);
                }
                System.out.println("Command at index " + commandtype + ": " + Mnemonic.OPCODE[iterator.byteAt(commandtype)] + "   * " + iterator.byteAt(commandtype));
                for(int dat : data)
                {
                    System.out.println("  * " + dat);
                }
            }
        }
    }

    public static CtClass addEmptyConstructor(String name) throws NotFoundException, CannotCompileException, IOException {
        CtClass ctClass = ClassPool.getDefault().getCtClass(name);
        ctClass.defrost();
        ClassFile classFile = ctClass.getClassFile();
        MethodInfo newMethod = new MethodInfo(classFile.getConstPool(), "<init>", "()V");
        newMethod.setCodeAttribute(new CodeAttribute(classFile.getConstPool(),1,1,new byte[]{0,0,0,0,0},new ExceptionTable(classFile.getConstPool())));
        CodeIterator iterator = newMethod.getCodeAttribute().iterator();
        iterator.writeByte(42, 0);
        iterator.writeByte(183,1);
        iterator.writeByte(0,2);
        iterator.writeByte(1,3);
        iterator.writeByte(177,4);
        classFile.addMethod(newMethod);
        return ctClass;
    }

    public static Class finalizeChanges(CtClass finalize) throws CannotCompileException {
        finalize.defrost();
        return finalize.toClass();
    }

}
