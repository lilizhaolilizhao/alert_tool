package com.taobao.arthas.core.command.monitor200;

import com.taobao.middleware.cli.annotations.Description;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Summary;

/**
 * 监控请求命令<br/>
 * @author vlinux
 */
@Name("monitor")
@Summary("Monitor method execution statistics, e.g. total/success/failure count, average rt, fail rate, etc. ")
@Description("\nExamples:\n" +
        "  monitor org.apache.commons.lang.StringUtils isBlank\n" +
        "  monitor org.apache.commons.lang.StringUtils isBlank -c 5\n" +
        "  monitor -E org\\.apache\\.commons\\.lang\\.StringUtils isBlank\n")
public class MonitorCommand extends EnhancerCommand {
}
