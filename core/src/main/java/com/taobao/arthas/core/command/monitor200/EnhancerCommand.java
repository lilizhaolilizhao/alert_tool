package com.taobao.arthas.core.command.monitor200;

import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.shell.handlers.command.CommandInterruptHandler;
import com.taobao.arthas.core.util.LogUtil;
import com.taobao.arthas.core.util.matcher.Matcher;
import com.taobao.middleware.logger.Logger;

import java.util.Collections;
import java.util.List;

public abstract class EnhancerCommand extends AnnotatedCommand {

    private static final Logger logger = LogUtil.getArthasLogger();
    private static final int SIZE_LIMIT = 50;
    private static final int MINIMAL_COMPLETE_SIZE = 3;
    protected static final List<String> EMPTY = Collections.emptyList();
    private static final String[] EXPRESS_EXAMPLES = {"params", "returnObj", "throwExp", "target", "clazz", "method",
            "{params,returnObj}", "params[0]"};

    protected Matcher classNameMatcher;
    protected Matcher methodNameMatcher;

    /**
     * 类名匹配
     *
     * @return 获取类名匹配
     */
    protected abstract Matcher getClassNameMatcher();

    /**
     * 方法名匹配
     *
     * @return 获取方法名匹配
     */
    protected abstract Matcher getMethodNameMatcher();

    @Override
    public void process(CommandProcess process) {
        // ctrl-C support
        process.interruptHandler(new CommandInterruptHandler(process));

    }
}
