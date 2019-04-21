package com.taobao.arthas.core.advisor;

import com.taobao.arthas.core.GlobalOptions;
import com.taobao.arthas.core.util.ArthasCheckUtils;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.util.affect.EnhancerAffect;
import com.taobao.arthas.core.util.matcher.Matcher;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.commons.Method;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知编织者
 * 线程帧栈与执行帧栈
 * 编织者在执行通知的时候有两个重要的栈:线程帧栈(threadFrameStack),执行帧栈(frameStack)
 */
public class AdviceWeaver extends ClassVisitor implements Opcodes {
    // 通知监听器集合
    private final static Map<Integer/*ADVICE_ID*/, AdviceListener> advices
            = new ConcurrentHashMap<Integer, AdviceListener>();

    private final int adviceId;
    private final boolean isTracing;
    private final boolean skipJDKTrace;
    private final String className;
    private String superName;
    private final Matcher matcher;
    private final EnhancerAffect affect;

    /**
     * 构建通知编织器
     *
     * @param adviceId  通知ID
     * @param isTracing 可跟踪方法调用
     * @param className 类名称
     * @param matcher   方法匹配
     *                  只有匹配上的方法才会被织入通知器
     * @param affect    影响计数
     * @param cv        ClassVisitor for ASM
     */
    public AdviceWeaver(int adviceId, boolean isTracing, boolean skipJDKTrace, String className, Matcher matcher, EnhancerAffect affect, ClassVisitor cv) {
        super(ASM5, cv);
        this.adviceId = adviceId;
        this.isTracing = isTracing;
        this.skipJDKTrace = skipJDKTrace;
        this.className = className;
        this.matcher = matcher;
        this.affect = affect;
    }

    /**
     * 方法开始<br/>
     * 用于编织通知器,外部不会直接调用
     *
     * @param loader     类加载器
     * @param adviceId   通知ID
     * @param className  类名
     * @param methodName 方法名
     * @param methodDesc 方法描述
     * @param target     返回结果
     *                   若为无返回值方法(void),则为null
     * @param args       参数列表
     */
    public static void methodOnBegin(
            int adviceId,
            ClassLoader loader, String className, String methodName, String methodDesc,
            Object target, Object[] args) {

    }

    /**
     * 方法以返回结束<br/>
     * 用于编织通知器,外部不会直接调用
     *
     * @param returnObject 返回对象
     *                     若目标为静态方法,则为null
     */
    public static void methodOnReturnEnd(Object returnObject) {

    }

    /**
     * 方法以抛异常结束<br/>
     * 用于编织通知器,外部不会直接调用
     *
     * @param throwable 抛出异常
     */
    public static void methodOnThrowingEnd(Throwable throwable) {

    }

    /**
     * 方法内部调用开始
     *
     * @param adviceId 通知ID
     * @param owner    调用类名
     * @param name     调用方法名
     * @param desc     调用方法描述
     */
    public static void methodOnInvokeBeforeTracing(int adviceId, String owner, String name, String desc) {
    }

    /**
     * 方法内部调用结束(正常返回)
     *
     * @param adviceId 通知ID
     * @param owner    调用类名
     * @param name     调用方法名
     * @param desc     调用方法描述
     */
    public static void methodOnInvokeAfterTracing(int adviceId, String owner, String name, String desc) {
    }

    /**
     * 方法内部调用结束(异常返回)
     *
     * @param adviceId 通知ID
     * @param owner    调用类名
     * @param name     调用方法名
     * @param desc     调用方法描述
     */
    public static void methodOnInvokeThrowTracing(int adviceId, String owner, String name, String desc) {
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.superName = superName;
    }

    @Override
    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String desc,
            final String signature,
            final String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        if (isIgnore(mv, access, name)) {
            return mv;
        }

        // 编织方法计数
        affect.mCnt(1);

        return new AdviceAdapter(ASM5, new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions), access, name, desc) {
            // -- Label for try...catch block
            private final Label beginLabel = new Label();
            private final Label endLabel = new Label();

            // -- KEY of advice --
            private final int KEY_ARTHAS_ADVICE_BEFORE_METHOD = 0;
            private final int KEY_ARTHAS_ADVICE_RETURN_METHOD = 1;
            private final int KEY_ARTHAS_ADVICE_THROWS_METHOD = 2;
            private final int KEY_ARTHAS_ADVICE_BEFORE_INVOKING_METHOD = 3;
            private final int KEY_ARTHAS_ADVICE_AFTER_INVOKING_METHOD = 4;
            private final int KEY_ARTHAS_ADVICE_THROW_INVOKING_METHOD = 5;

            // -- KEY of ASM_TYPE or ASM_METHOD --
            private final Type ASM_TYPE_SPY = Type.getType("Ljava/arthas/Spy;");
            private final Type ASM_TYPE_OBJECT = Type.getType(Object.class);
            private final Type ASM_TYPE_OBJECT_ARRAY = Type.getType(Object[].class);
            private final Type ASM_TYPE_CLASS = Type.getType(Class.class);
            private final Type ASM_TYPE_INTEGER = Type.getType(Integer.class);
            private final Type ASM_TYPE_CLASS_LOADER = Type.getType(ClassLoader.class);
            private final Type ASM_TYPE_STRING = Type.getType(String.class);
            private final Type ASM_TYPE_THROWABLE = Type.getType(Throwable.class);
            private final Type ASM_TYPE_INT = Type.getType(int.class);
            private final Type ASM_TYPE_METHOD = Type.getType(java.lang.reflect.Method.class);
            private final Method ASM_METHOD_METHOD_INVOKE = Method.getMethod("Object invoke(Object,Object[])");

            // 代码锁
            private final CodeLock codeLockForTracing = new TracingAsmCodeLock(this);

            private void _debug(final StringBuilder append, final String msg) {
                if (!GlobalOptions.isDebugForAsm) {
                    return;
                }

                // println msg
                visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                if (StringUtils.isBlank(append.toString())) {
                    visitLdcInsn(append.append(msg).toString());
                } else {
                    visitLdcInsn(append.append(" >> ").append(msg).toString());
                }

                visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            }

            /**
             * 加载通知方法
             * @param keyOfMethod 通知方法KEY
             */
            private void loadAdviceMethod(int keyOfMethod) {
                switch (keyOfMethod) {
                    case KEY_ARTHAS_ADVICE_BEFORE_METHOD: {
                        getStatic(ASM_TYPE_SPY, "ON_BEFORE_METHOD", ASM_TYPE_METHOD);
                        break;
                    }

                    case KEY_ARTHAS_ADVICE_RETURN_METHOD: {
                        getStatic(ASM_TYPE_SPY, "ON_RETURN_METHOD", ASM_TYPE_METHOD);
                        break;
                    }

                    case KEY_ARTHAS_ADVICE_THROWS_METHOD: {
                        getStatic(ASM_TYPE_SPY, "ON_THROWS_METHOD", ASM_TYPE_METHOD);
                        break;
                    }

                    case KEY_ARTHAS_ADVICE_BEFORE_INVOKING_METHOD: {
                        getStatic(ASM_TYPE_SPY, "BEFORE_INVOKING_METHOD", ASM_TYPE_METHOD);
                        break;
                    }

                    case KEY_ARTHAS_ADVICE_AFTER_INVOKING_METHOD: {
                        getStatic(ASM_TYPE_SPY, "AFTER_INVOKING_METHOD", ASM_TYPE_METHOD);
                        break;
                    }

                    case KEY_ARTHAS_ADVICE_THROW_INVOKING_METHOD: {
                        getStatic(ASM_TYPE_SPY, "THROW_INVOKING_METHOD", ASM_TYPE_METHOD);
                        break;
                    }

                    default: {
                        throw new IllegalArgumentException("illegal keyOfMethod=" + keyOfMethod);
                    }
                }
            }

            private void loadClassLoader() {
                if (this.isStaticMethod()) {
                    visitLdcInsn(StringUtils.normalizeClassName(className));
                    invokeStatic(ASM_TYPE_CLASS, Method.getMethod("Class forName(String)"));
                    invokeVirtual(ASM_TYPE_CLASS, Method.getMethod("ClassLoader getClassLoader()"));
                } else {
                    loadThis();
                    invokeVirtual(ASM_TYPE_OBJECT, Method.getMethod("Class getClass()"));
                    invokeVirtual(ASM_TYPE_CLASS, Method.getMethod("ClassLoader getClassLoader()"));
                }
            }

            /**
             * 是否静态方法
             * @return true:静态方法 / false:非静态方法
             */
            private boolean isStaticMethod() {
                return (methodAccess & ACC_STATIC) != 0;
            }

            /**
             * 加载this/null
             */
            private void loadThisOrPushNullIfIsStatic() {
                if (isStaticMethod()) {
                    pushNull();
                } else {
                    loadThis();
                }
            }

            /**
             * 加载before通知参数数组
             */
            private void loadArrayForBefore() {
                //new Object[7]
                push(7);
                newArray(ASM_TYPE_OBJECT);

                //object[0] = adviceID
                dup();
                push(0);
                push(adviceId);
                box(ASM_TYPE_INT);
                arrayStore(ASM_TYPE_INTEGER);

                //object[1] = classloader
                dup();
                push(1);
                loadClassLoader();
                arrayStore(ASM_TYPE_CLASS_LOADER);

                //objec[2] = className
                dup();
                push(2);
                push(className);
                arrayStore(ASM_TYPE_STRING);

                //object[3] = methodName
                dup();
                push(3);
                push(name);
                arrayStore(ASM_TYPE_STRING);

                //object[4] = method Desc
                dup();
                push(4);
                push(desc);
                arrayStore(ASM_TYPE_STRING);

                //object[5] = this or null
                dup();
                push(5);
                loadThisOrPushNullIfIsStatic();
                arrayStore(ASM_TYPE_OBJECT);

                //object[6] = args
                dup();
                push(6);
                loadArgArray();
                arrayStore(ASM_TYPE_OBJECT_ARRAY);
            }

            @Override
            protected void onMethodEnter() {
                codeLockForTracing.lock(new CodeLock.Block() {
                    @Override
                    public void code() {
                        final StringBuilder append = new StringBuilder();
                        _debug(append, "debug:onMethodEnter()");

                        // 加载before方法
                        loadAdviceMethod(KEY_ARTHAS_ADVICE_BEFORE_METHOD);

                        _debug(append, "debug:onMethodEnter() > loadAdviceMethod()");

                        // 推入Method.invoke()的第一个参数
                        pushNull();

                        // 方法参数
                        loadArrayForBefore();

                        _debug(append, "debug:onMethodEnter() > loadAdviceMethod() > loadArrayForBefore()");

                        // 调用方法
                        invokeVirtual(ASM_TYPE_METHOD, ASM_METHOD_METHOD_INVOKE);
                        pop();

                        _debug(append, "debug:onMethodEnter() > loadAdviceMethod() > loadArrayForBefore() > invokeVirtual()");
                    }
                });

                mark(beginLabel);
            }

            /**
             * 将NULL推入堆栈
             */
            private void pushNull() {
                push((Type) null);
            }
        };
    }

    /**
     * 是否抽象属性
     */
    private boolean isAbstract(int access) {
        return (ACC_ABSTRACT & access) == ACC_ABSTRACT;
    }

    /**
     * 是否需要忽略
     */
    private boolean isIgnore(MethodVisitor mv, int access, String methodName) {
        return null == mv
                || isAbstract(access)
                || !matcher.matching(methodName)
                || ArthasCheckUtils.isEquals(methodName, "<clinit>");
    }

    /**
     * 注册监听器
     *
     * @param adviceId 通知ID
     * @param listener 通知监听器
     */
    public static void reg(int adviceId, AdviceListener listener) {

        // 触发监听器创建
        listener.create();

        // 注册监听器
        advices.put(adviceId, listener);
    }

    /**
     * 注销监听器
     *
     * @param adviceId 通知ID
     */
    public static void unReg(int adviceId) {

        // 注销监听器
        final AdviceListener listener = advices.remove(adviceId);

        // 触发监听器销毁
        if (null != listener) {
            listener.destroy();
        }
    }

    /**
     * 恢复监听
     *
     * @param adviceId 通知ID
     * @param listener 通知监听器
     */
    public static void resume(int adviceId, AdviceListener listener) {
        // 注册监听器
        advices.put(adviceId, listener);
    }

    /**
     * 暂停监听
     *
     * @param adviceId 通知ID
     */
    public static AdviceListener suspend(int adviceId) {
        // 注销监听器
        return advices.remove(adviceId);
    }
}
