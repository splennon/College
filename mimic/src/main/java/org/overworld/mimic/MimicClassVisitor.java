package org.overworld.mimic;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.overworld.mimic.config.MethodMatch;

/**
 * The Class Visitor to be used in the ASM Visitor Pattern that instruments the
 * bytecode
 *
 * @author Stephen Lennon stephen@overworld.org
 *
 *         Date: 2015-09-26
 */
public class MimicClassVisitor extends ClassVisitor {

    private String className;
    @SuppressWarnings("unused")
    private String classSignature;
    private final List<MethodMatch> methodMatches = new ArrayList<MethodMatch>();
    private final AgentMode mode;

    public MimicClassVisitor(final ClassWriter writer,
        final List<MethodMatch> methodMatches, final AgentMode mode) {

        super(Opcodes.ASM5, writer);

        if (methodMatches != null)
            this.methodMatches.addAll(methodMatches);

        this.mode = mode;
    }

    @Override
    public void visit(final int version, final int access,
        final String className, final String classSignature,
        final String superName, final String[] interfaces) {

        super.visit(version, access, className, classSignature, superName,
            interfaces);

        this.className = className;
        this.classSignature = classSignature;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
        final String desc, final String signature, final String[] exceptions) {

        MethodVisitor mv = super.visitMethod(access, name, desc, signature,
            exceptions);

        boolean proceed = false;

        for (final MethodMatch mm : this.methodMatches) {

            if (mm.matches(name)) {
                if (mm.matchesSignature(desc)) {
                    proceed = true;
                    break;
                }
            }
        }

        final boolean isNative = (access & Opcodes.ACC_NATIVE) != 0;
        final boolean isAbstract = (access & Opcodes.ACC_ABSTRACT) != 0;
        final boolean isSynthetic = (access & Opcodes.ACC_SYNTHETIC) != 0;

        if (!proceed || isNative || isAbstract || isSynthetic) return mv;

        /*
         * Thread.start() has special handling for tracking threads in the VM
         * and so is subject to special instrumentation. No other method of
         * thread can be instrumented
         */

        if (name.equals("start") && this.className.equals("java/lang/Thread")
            && desc.equals("()V")) {

            switch (this.mode) {

            case RECORD:

                return new ThreadRecordMethodVisitor(mv, access,
                    this.className, name, desc, exceptions);
            case REPLAY:

                return new ThreadReplayMethodVisitor(mv, access,
                    this.className, name, desc, exceptions);
            }
        } else if (this.className.equals("java/lang/Thread")) {

            return mv;
        }

        LocalVariablesSorter lvs;

        switch (this.mode) {

        case RECORD:

            final RecordMethodVisitor recordMv = new RecordMethodVisitor(mv,
                access, this.className, name, desc, exceptions);
            lvs = new LocalVariablesSorter(access, desc, recordMv);
            recordMv.setLvs(lvs);
            mv = recordMv;

            System.err.println(String.format("Record Instrumenting %s %s %s",
                this.className, name, desc)); // TODO

            break;

        case REPLAY:

            final ReplayMethodVisitor replayMv = new ReplayMethodVisitor(mv,
                access, this.className, name, desc, exceptions);
            lvs = new LocalVariablesSorter(access, desc, replayMv);
            replayMv.setLvs(lvs);
            mv = replayMv;

            System.err.println(String.format("Replay Instrumenting %s %s %s",
                this.className, name, desc)); // TODO

            break;

        default:

            throw new AssertionError("Unexpected code path");
        }

        return mv;
    }
}
