package app;

import minispring.annotation.MiniAutowired;
import minispring.annotation.MiniComponent;

import java.time.Clock;

@MiniComponent
public class UserService {
    @MiniAutowired
    private Clock clock;

    @MiniAutowired
    private MemoService memoService;

    public void createUser(String name) {
        System.out.println("User created: " + name + " at " + clock.instant());
        System.out.println("memo write: " + memoService.write(name + " created"));
    }
}