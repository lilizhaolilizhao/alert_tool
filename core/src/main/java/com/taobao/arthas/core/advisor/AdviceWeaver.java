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
