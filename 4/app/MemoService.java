package app;

import minispring.annotation.MiniComponent;
import java.time.LocalDateTime;

@MiniComponent
public class MemoService {
    public String write(String content) {
        return "[" + LocalDateTime.now() + "] " + content;
    }
}