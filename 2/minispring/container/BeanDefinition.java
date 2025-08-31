package minispring.container;

public class BeanDefinition {
    private final Object instance;
    private final String qualifier;
    
    public BeanDefinition(Object instance, String qualifier) {
        this.instance = instance;
        this.qualifier = qualifier;
    }
    
    public Object getInstance() {
        return instance;
    }
    
    public String getQualifier() {
        return qualifier;
    }
    
    public boolean hasQualifier() {
        return qualifier != null && !qualifier.isEmpty();
    }
    
    public boolean matchesQualifier(String targetQualifier) {
        if (targetQualifier == null || targetQualifier.isEmpty()) {
            return !hasQualifier(); // 기본 빈 매칭
        }
        return targetQualifier.equals(qualifier);
    }
}