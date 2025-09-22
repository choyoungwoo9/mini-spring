package minispring.container;

public class BeanDefinition {
    private Object instance;
    private final String qualifier;
    private final boolean primary;
    
    public BeanDefinition(Object instance, String qualifier) {
        this(instance, qualifier, false);
    }
    
    public BeanDefinition(Object instance, String qualifier, boolean primary) {
        this.instance = instance;
        this.qualifier = qualifier;
        this.primary = primary;
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
    
    public boolean isPrimary() {
        return primary;
    }
    
    public void setInstance(Object instance) {
        this.instance = instance;
    }
}