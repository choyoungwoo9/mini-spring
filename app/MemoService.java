package app;

import minispring.annotation.MiniAutowired;
import minispring.annotation.MiniComponent;

import java.time.Clock;

@MiniComponent
public class MemoService {
    @MiniAutowired
    Clock clock;
    public String write(String content) { return "[" + clock.instant() + "] " + content; }
}