package app;

import minispring.container.MiniApplicationContext;

public class Main {
    public static void main(String[] args) {
        var ctx = new MiniApplicationContext(AppConfig.class);
        var userService = ctx.getBean(UserService.class);
        userService.createUser("new User");
    }
}