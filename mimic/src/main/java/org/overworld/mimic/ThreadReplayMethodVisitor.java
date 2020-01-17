package org.overworld.mimic;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * The Method Visitor to be used in the ASM Visitor Pattern that instruments the
 * bytecode of the Thread.start() while replay
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2015-09-26
 */
public class ThreadReplayMethodVisitor extends AdviceAdapter {

    /**
     * Construct a visitor instance to apply to the Thread.start()
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
    public ThreadReplayMethodVisitor(final MethodVisitor mv, final int access,
        final String className, final String methodName,
        final String methodDesc, final String[] exceptions) {

        super(Opcodes.ASM5, mv, access, methodName, methodDesc);
    }

    /**
     * Applies instrumentation on method entry
     */
    @Override
    protected void onMethodEnter() {

        this.visitVarInsn(Opcodes.ALOAD, 0);

        this.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Thread",
            "getId", "()J", false);

        this.visitMethodInsn(Opcodes.INVOKESTATIC,
            "org/overworld/mimic/Replay", "threadStart", "(J)V", false);
    }
}
