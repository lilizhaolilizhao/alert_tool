package com.taobao.arthas.core.advisor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

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

    public AdviceWeaver(int api) {
        super(ASM5);
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
