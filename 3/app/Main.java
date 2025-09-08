package app;

import minispring.container.MiniApplicationContext;

public class Main {
    public static void main(String[] args) {
        var ctx = new MiniApplicationContext(AppConfig.class);
        var memo = ctx.getBean(MemoService.class);
        memo.write("aop!");
        // 목표: [AOP] before와 [AOP] after가 출력되어야 함
    }
}
