package com.taobao.arthas.core.command.klass100;

import org.junit.Test;

import java.util.regex.Pattern;

public class JadCommandTest {

    @Test
    public void testDecompile() {

    }

    @Test
    public void testPattern(String[] args) {
        String[] names = {
                "com.taobao.container.web.arthas.mvc.AppInfoController",
                "com.taobao.container.web.arthas.mvc.AppInfoController$1$$Lambda$19/381016128",
                "com.taobao.container.web.arthas.mvc.AppInfoController$$Lambda$16/17741163",
                "com.taobao.container.web.arthas.mvc.AppInfoController$1",
                "com.taobao.container.web.arthas.mvc.AppInfoController$123",
                "com.taobao.container.web.arthas.mvc.AppInfoController$A",
                "com.taobao.container.web.arthas.mvc.AppInfoController$ABC"
        };

        String pattern = "com.taobao.container.web.arthas.mvc.AppInfoController" + "(?!.*\\$\\$Lambda\\$).*";
        for (String name : names) {
            System.out.println(name + "    " + Pattern.matches(pattern, name));
        }
    }
}
