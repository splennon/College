package org.overworld.mimic;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;

/**
 * The Method Visitor to be used in the ASM Visitor Pattern that instruments the
 * bytecode while replaying
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2015-09-26
 */
public class ReplayMethodVisitor extends AdviceAdapter {

    private final Type[] argTypes;

    private final String className, methodName, methodDesc;

    private final boolean isStatic;

    private LocalVariablesSorter lvs;

    private final Type returnType;

    /**
     * Construct a visitor instance to apply to one method
     *
     * @param mv
     *            the chained method visitor
     * @param access
     *            the access permissions of the method
     * @param className
     *            the name of the containing class
     * @param methodName
     *            the name of this visited method
     * @param methodDesc
     *            the description of this instrumented method
     * @param exceptions
     *            exceptions thrown by this instrumented method
     */
    public ReplayMethodVisitor(final MethodVisitor mv, final int access,
        final String className, final String methodName,
        final String methodDesc, final String[] exceptions) {

        super(Opcodes.ASM5, mv, access, methodName, methodDesc);

        this.argTypes = Type.getArgumentTypes(methodDesc);
        this.returnType = Type.getReturnType(methodDesc);
        this.className = className;
        this.methodName = methodName;
        this.methodDesc = methodDesc;

        this.isStatic = (access & Opcodes.ACC_STATIC) != 0;
    }

    /**
     * @return the LocalVariableSorter set in this instance
     */
    public LocalVariablesSorter getLvs() {

        return this.lvs;
    }

    /**
     * Applies instrumentation on method entry
     */
    @Override
    protected void onMethodEnter() {

        final int argsLength = this.argTypes.length;

        /* create an array the size of the arguments */

        /* push the array length onto stack as parameter to newarray */
        super.visitIntInsn(Opcodes.BIPUSH, argsLength);

        /* call newarray to make an array of Object */
        super.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");

        /*
         * create a new local variable to hold the array of objects representing
         * the method parameters and metadata
         */
        final int local_array = this.getLvs().newLocal(
            Type.getType("[Ljava/lang/Object;"));

        /*
         * take the reference returned from newarray and put it in this new
         * local variable
         */
        super.visitVarInsn(Opcodes.ASTORE, local_array);

        /*
         * the offset in the parameters array, may increment by 1 or 2 depending
         * on the parameter type non-static methods have a this reference at the
         * start of the parameters so these begin at 1, static methods begin at
         * 0
         */
        int param_offset = this.isStatic ? 0 : 1;

        /*
         * the target index to load the resulting boxed variable or reference
         * into, only ever increments by one
         */
        int array_index = 0;

        for (final Type type : this.argTypes) {

            /*
             * Send the parameters of the method to Replay.entry for
             * (Potentially) accounting and comparison purposes
             */

            /* load the local array onto stack */
            super.visitVarInsn(Opcodes.ALOAD, local_array);

            /* push the target array index onto stack */
            super.visitIntInsn(Opcodes.BIPUSH, array_index);

            if (type.equals(Type.BOOLEAN_TYPE) || type.equals(Type.BYTE_TYPE)
                || type.equals(Type.CHAR_TYPE) || type.equals(Type.INT_TYPE)
                || type.equals(Type.SHORT_TYPE)) {

                /* integral size types take up one slot */
                super.visitVarInsn(Opcodes.ILOAD, param_offset);
                super.box(type);
                param_offset++;
                array_index++;

            } else if (type.equals(Type.LONG_TYPE)) {

                /* long types take up two slots */
                super.visitVarInsn(Opcodes.LLOAD, param_offset);
                super.box(type);
                param_offset += 2;
                array_index++;

            } else if (type.equals(Type.DOUBLE_TYPE)) {

                /* double types take up two slots */
                super.visitVarInsn(Opcodes.DLOAD, param_offset);
                super.box(type);
                param_offset += 2;
                array_index++;

            } else if (type.equals(Type.FLOAT_TYPE)) {

                /* float types take up one slot */
                super.visitVarInsn(Opcodes.FLOAD, param_offset);
                super.box(type);
                param_offset++;
                array_index++;

            } else {

                /* object (Other) types take up one slot */
                super.visitVarInsn(Opcodes.ALOAD, param_offset);
                param_offset++;
                array_index++;

            }

            super.visitInsn(Opcodes.AASTORE);
        }

        /* load metadata parameters to go to static accounting function */

        super.visitLdcInsn(this.className);
        super.visitLdcInsn(this.methodName);
        super.visitLdcInsn(this.methodDesc);
        super.visitVarInsn(Opcodes.ALOAD, local_array);

        /* @formatter:off */
        super.visitMethodInsn(Opcodes.INVOKESTATIC,
            "org/overworld/mimic/Replay", "entry",
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V",
            false);
        /* @formatter:on */

        /*
         * Put the recorded return value onto the stack
         */

        super.visitMethodInsn(Opcodes.INVOKESTATIC,
            "org/overworld/mimic/Replay", "getRetVal", "()Ljava/lang/Object;",
            false);

        /* check for a throw instead of a return */

        super.visitMethodInsn(Opcodes.INVOKESTATIC,
            "org/overworld/mimic/Replay", "exit", "()I", false);

        /* original return opcode is on the stack from exit() */

        super.visitIntInsn(Opcodes.SIPUSH, Opcodes.ATHROW);
        final Label skipThrow = new Label();
        super.visitJumpInsn(Opcodes.IF_ICMPNE, skipThrow);

        super.visitTypeInsn(Opcodes.CHECKCAST, Type
            .getInternalName(Throwable.class));
        super.visitInsn(Opcodes.ATHROW);

        super.visitLabel(skipThrow);

        /*
         * Unbox the value if the method return type is not a reference type or
         * void
         */

        int returnOpcode = -1;

        switch (this.returnType.getSort()) {
        case Type.BOOLEAN:
        case Type.BYTE:
        case Type.CHAR:
        case Type.INT:
        case Type.SHORT:
            /* ireturn */
            super.unbox(this.returnType);
            returnOpcode = Opcodes.IRETURN;
            break;
        case Type.LONG:
            /* lreturn */
            super.unbox(this.returnType);
            returnOpcode = Opcodes.LRETURN;
            break;
        case Type.DOUBLE:
            /* dreturn */
            super.unbox(this.returnType);
            returnOpcode = Opcodes.DRETURN;
            break;
        case Type.FLOAT:
            /* freturn */
            super.unbox(this.returnType);
            returnOpcode = Opcodes.FRETURN;
            break;
        case Type.VOID:
            /* return */
            returnOpcode = Opcodes.RETURN;
            break;
        case Type.OBJECT:
            /* areturn */
            super.visitTypeInsn(Opcodes.CHECKCAST, this.returnType
                .getInternalName());
            returnOpcode = Opcodes.ARETURN;
        }

        super.visitInsn(returnOpcode);
    }

    /**
     * @param lvs
     *            the LocalVariableSorter to set on this instance
     */
    public void setLvs(final LocalVariablesSorter lvs) {

        this.lvs = lvs;
    }

    /**
     * Visit Field Instruction. Bytecode is removed from the instrumented method
     * by this empty visitor method
     */
    @Override
    public void visitFieldInsn(final int opcode, final String owner,
        final String name, final String desc) {

    };

    /**
     * Visit Instruction. Bytecode is removed from the instrumented method by
     * this empty visitor method
     */
    @Override
    public void visitInsn(final int opcode) {

    }

    /**
     * Visit Int Instruction. Bytecode is removed from the instrumented method
     * by this empty visitor method
     */
    @Override
    public void visitIntInsn(final int opcode, final int operand) {

    }

    /**
     * Visit Invoke Dynamic Instruction. Bytecode is removed from the
     * instrumented method by this empty visitor method
     */
    @Override
    public void visitInvokeDynamicInsn(final String name, final String desc,
        final Handle bsm, final Object... bsmArgs) {

    }

    /**
     * Visit Jump Instruction. Bytecode is removed from the instrumented method
     * by this empty visitor method
     */
    @Override
    public void visitJumpInsn(final int opcode, final Label label) {

    }

    /**
     * Visit Label. Bytecode is removed from the instrumented method by this
     * empty visitor method
     */
    @Override
    public void visitLabel(final Label label) {

    }

    /**
     * Visit LDC Instruction. Bytecode is removed from the instrumented method
     * by this empty visitor method
     */
    @Override
    public void visitLdcInsn(final Object cst) {

    }

    /**
     * Visit Line Number. Bytecode is removed from the instrumented method by
     * this empty visitor method
     */
    @Override
    public void visitLineNumber(final int line, final Label start) {

    }

    /**
     * Visit Lookup Switch Instruction. Bytecode is removed from the
     * instrumented method by this empty visitor method
     */
    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
        final Label[] labels) {

    }

    /**
     * Visit Method Instruction. Bytecode is removed from the instrumented
     * method by this empty visitor method
     */
    @Override
    public void visitMethodInsn(final int opcode, final String owner,
        final String name, final String desc) {

    }

    /**
     * Visit Method Instruction. Bytecode is removed from the instrumented
     * method by this empty visitor method
     */
    @Override
    public void visitMethodInsn(final int opcode, final String owner,
        final String name, final String desc, final boolean itf) {

    }

    /**
     * Visit Multidimensional Array Instruction. Bytecode is removed from the
     * instrumented method by this empty visitor method
     */
    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {

    }

    /**
     * Visit Table Switch Instruction. Bytecode is removed from the instrumented
     * method by this empty visitor method
     */
    @Override
    public void visitTableSwitchInsn(final int min, final int max,
        final Label dflt, final Label... labels) {

    }

    /**
     * Visit Try Catch Instruction. Bytecode is removed from the instrumented
     * method by this empty visitor method
     */
    @Override
    public void visitTryCatchBlock(final Label start, final Label end,
        final Label handler, final String type) {

    }

    /**
     * Visit Type Instruction. Bytecode is removed from the instrumented method
     * by this empty visitor method
     */
    @Override
    public void visitTypeInsn(final int opcode, final String type) {

    }

    /**
     * Visit Var Instruction. Bytecode is removed from the instrumented method
     * by this empty visitor method
     */
    @Override
    public void visitVarInsn(final int opcode, final int var) {

    }
}
