package app.memo;

import minispring.annotation.MiniComponent;

@MiniComponent
public class MemoService {
    
    public void write(String content) {
        System.out.println("메모 작성: " + content);
    }
    
    public String read(String id) {
        System.out.println("메모 읽기: " + id);
        return "메모 내용: " + id;
    }
    
    public void delete(String id) {
        System.out.println("메모 삭제: " + id);
    }
}
