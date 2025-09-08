package app;

public interface MemoServiceInterface {
    void save(String content);
    String read(String id);
    void delete(String id);
}
