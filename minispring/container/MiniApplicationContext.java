package minispring.container;

public class MiniApplicationContext {
    public MiniApplicationContext(Class<?> configClass) {
        //컴포넌트 스캔 후 빈 등록
        //빈 찾을 수 없거나, 모호하면 에러
    }

    public <T> T getBean(Class<T> type){
        return null;
    }
}
