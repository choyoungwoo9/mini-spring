package app;

import minispring.annotation.MiniComponent;

@MiniComponent
public class MemoService implements MemoServiceInterface {
    
    @Override
    public void save(String content) {
        System.out.println("메모 저장: " + content);
    }
    
    @Override
    public String read(String id) {
        System.out.println("메모 읽기: " + id);
        return "메모 내용: " + id;
    }
    
    @Override
    public void delete(String id) {
        System.out.println("메모 삭제: " + id);
    }
}
