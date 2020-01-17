package org.overworld.mimic;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;

/**
 * The Method Visitor to be used in the ASM Visitor Pattern that instruments the
 * bytecode while recording
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2015-09-26
 */
public class RecordMethodVisitor extends AdviceAdapter {

    private final Type[] argTypes;

    private final String className, methodName, methodDesc;

    private final boolean isStatic;

    private LocalVariablesSorter lvs;

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
    public RecordMethodVisitor(final MethodVisitor mv, final int access,
        final String className, final String methodName,
        final String methodDesc, final String[] exceptions) {

        super(Opcodes.ASM5, mv, access, methodName, methodDesc);

        this.argTypes = Type.getArgumentTypes(methodDesc);
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

            this.mv.visitInsn(Opcodes.AASTORE);
        }

        /* load metadata parameters to go to static accounting function */

        super.visitLdcInsn(this.className);
        super.visitLdcInsn(this.methodName);
        super.visitLdcInsn(this.methodDesc);
        super.visitVarInsn(Opcodes.ALOAD, local_array);

        /* @formatter:off */
        super.visitMethodInsn(Opcodes.INVOKESTATIC,
            "org/overworld/mimic/Record", "entry",
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)V",
            false);
        /* @formatter:on */
    }

    /**
     * Applies instrumentation on method exit
     */
    @Override
    protected void onMethodExit(final int opcode) {

        /* push the return value, boxed if necessary */

        switch (opcode) {
        case RETURN:
            /* for a void return put null on the stack */
            super.visitInsn(Opcodes.ACONST_NULL);
            break;
        case ARETURN:
        case ATHROW:
            /*
             * for a reference return or throw: duplicate a single stack frame.
             * A duplicate is sent to the accounting method, and the original
             * remains unmolested on the stack
             */
            super.dup();
            break;
        case LRETURN:
        case DRETURN:
            /*
             * for double sized primitives, duplicate and box them, leaving a
             * reference behind for the accounting method
             */
            super.dup2();
            super.box(Type.getReturnType(this.methodDesc));
            break;
        case IRETURN:
        case FRETURN:
            /*
             * for single sized primitives, duplicate and box them, leaving a
             * reference behind for the accounting method
             */
            super.dup();
            super.box(Type.getReturnType(this.methodDesc));
            break;
        default:
            throw new MimicException("Unknown return type");
        }

        /* add the type of the return */

        super.visitIntInsn(Opcodes.SIPUSH, opcode);

        /* @formatter:off */
        super.visitMethodInsn(
            Opcodes.INVOKESTATIC, "org/overworld/mimic/Record", "exit",
            "(Ljava/lang/Object;I)V",
            false);
        /* @formatter:on */
    }

    /**
     * @param lvs
     *            the LocalVariableSorter to set on this instance
     */
    public void setLvs(final LocalVariablesSorter lvs) {

        this.lvs = lvs;
    }
}
