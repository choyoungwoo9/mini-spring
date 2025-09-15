package app;

import minispring.annotation.*;
import java.util.List;

@MiniController
@MiniRequestMapping("/memos")
public class MemoController {
    @MiniAutowired 
    MemoService memoService;

    @MiniGetMapping("/new")
    public MemoDto newMemo(@MiniRequestParam("content") String content) {
        return new MemoDto(memoService.write(content));
    }

    @MiniGetMapping
    public List<MemoDto> list() {
        return List.of(new MemoDto("demo1"), new MemoDto("demo2"));
    }
}