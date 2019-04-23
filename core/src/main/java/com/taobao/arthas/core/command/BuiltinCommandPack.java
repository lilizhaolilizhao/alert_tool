package com.taobao.arthas.core.command;

import com.taobao.arthas.core.command.basic1000.*;
import com.taobao.arthas.core.command.hidden.JulyCommand;
import com.taobao.arthas.core.command.hidden.OptionsCommand;
import com.taobao.arthas.core.command.klass100.*;
import com.taobao.arthas.core.command.monitor200.JvmCommand;
import com.taobao.arthas.core.command.monitor200.MonitorCommand;
import com.taobao.arthas.core.command.monitor200.StackCommand;
import com.taobao.arthas.core.command.monitor200.ThreadCommand;
import com.taobao.arthas.core.shell.command.Command;
import com.taobao.arthas.core.shell.command.CommandResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * 内建命令
 */
public class BuiltinCommandPack implements CommandResolver {

    private static List<Command> commands = new ArrayList<Command>();

    static {
        initCommands();
    }

    @Override
    public List<Command> commands() {
        return commands;
    }

    private static void initCommands() {
        commands.add(Command.create(HelpCommand.class));
        commands.add(Command.create(KeymapCommand.class));
        commands.add(Command.create(SearchClassCommand.class));
        commands.add(Command.create(SearchMethodCommand.class));
        commands.add(Command.create(ClassLoaderCommand.class));
        commands.add(Command.create(JadCommand.class));
        commands.add(Command.create(GetStaticCommand.class));
        commands.add(Command.create(MonitorCommand.class));
        commands.add(Command.create(StackCommand.class));
        commands.add(Command.create(ThreadCommand.class));
//        commands.add(Command.create(TraceCommand.class));
//        commands.add(Command.create(WatchCommand.class));
//        commands.add(Command.create(TimeTunnelCommand.class));
        commands.add(Command.create(JvmCommand.class));
//        commands.add(Command.create(DashboardCommand.class));
        commands.add(Command.create(DumpClassCommand.class));
        commands.add(Command.create(JulyCommand.class));
        commands.add(Command.create(OptionsCommand.class));
        commands.add(Command.create(ClsCommand.class));
        commands.add(Command.create(ResetCommand.class));
        commands.add(Command.create(VersionCommand.class));
        commands.add(Command.create(ShutdownCommand.class));
        commands.add(Command.create(SessionCommand.class));
        commands.add(Command.create(SystemPropertyCommand.class));
        commands.add(Command.create(RedefineCommand.class));
    }
}
