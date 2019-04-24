package com.taobao.arthas.core.command.monitor200;

import com.taobao.arthas.core.advisor.InvokeTraceable;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.StringUtils;

public class TraceAdviceListener extends AbstractTraceAdviceListener implements InvokeTraceable {

    /**
     * Constructor
     */
    public TraceAdviceListener(TraceCommand command, CommandProcess process) {
        super(command, process);
    }

    @Override
    public void invokeBeforeTracing(String tracingClassName, String tracingMethodName, String tracingMethodDesc) throws Throwable {
        threadBoundEntity.get().view.begin(
                StringUtils.normalizeClassName(tracingClassName) + ":" + tracingMethodName + "()");
    }

    @Override
    public void invokeThrowTracing(String tracingClassName, String tracingMethodName, String tracingMethodDesc) throws Throwable {
        threadBoundEntity.get().view.end("throws Exception");
    }

    @Override
    public void invokeAfterTracing(String tracingClassName, String tracingMethodName, String tracingMethodDesc) throws Throwable {
        threadBoundEntity.get().view.end();
    }
}
