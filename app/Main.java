package app;

import minispring.container.MiniApplicationContext;

public class Main {
    public static void main(String[] args) {
        var ctx = new MiniApplicationContext(AppConfig.class);
        var memo = ctx.getBean(MemoService.class);
        System.out.println(memo.write("hello")); // [2025-08-11T...] hello
    }
}