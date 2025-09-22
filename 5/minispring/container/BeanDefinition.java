package minispring.container;

public class BeanDefinition {
    private Object instance;
    private final String qualifier;
    private final boolean primary;
    private final Class<?> beanClass;
    
    public BeanDefinition(Object instance, String qualifier, Class<?> beanClass) {
        this(instance, qualifier, false, beanClass);
    }
    
    public BeanDefinition(Object instance, String qualifier, boolean primary, Class<?> beanClass) {
        this.instance = instance;
        this.qualifier = qualifier;
        this.primary = primary;
        this.beanClass = beanClass;
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
            return !hasQualifier();
        }
        return targetQualifier.equals(qualifier);
    }
    
    public boolean isPrimary() {
        return primary;
    }
    
    public void setInstance(Object instance) {
        this.instance = instance;
    }
    
    public Class<?> getBeanClass() {
        return beanClass;
    }
}
