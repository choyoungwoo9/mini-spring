package minispring.container;

import minispring.annotation.*;
import minispring.exception.NoSuchBeanException;
import minispring.exception.NoUniqueBeanException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class MiniApplicationContext {
    private final Map<Class<?>, List<BeanDefinition>> beans = new HashMap<>();
    private Object configInstance = null;

    public MiniApplicationContext(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(MiniConfiguration.class)) {
            //TODO: 전용 에러 클래스로 변경
            throw new RuntimeException("@MiniConfiguration annotation not found");
        }

        try {
            configInstance = configClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create config instance", e);
        }

        MiniComponentScan msc = configClass.getAnnotation(MiniComponentScan.class);
        if (msc == null) {
            //에러 대신 config 클래스 하위 경로를 사용할지 고민
            throw new RuntimeException("@MiniComponentScan annotation not found");
        }

        String packagePath = msc.value().replace('.', '/');
        if (Thread.currentThread().getContextClassLoader().getResource(packagePath) == null) {
            throw new RuntimeException("Package not found: " + packagePath + " (scan fail)");
        }

        //컴포넌트 스캔 후 컴포넌트 등록
        try {
            List<URL> urls = Collections.list(Thread.currentThread().getContextClassLoader().getResources(packagePath));
            for (URL url : urls) {
                String protocol = url.getProtocol();
                if (protocol.equals("file")) {
                    File directory = new File(url.getFile());
                    File[] files = directory.listFiles();
                    if (files == null)
                        continue;
                    for (File file : files) {
                        if (!file.getName().endsWith(".class"))
                            continue;
                        String className = file.getName().substring(0, file.getName().length() - 6);
                        String fullClassName = packagePath + "." + className;
                        try {
                            Class<?> clazz = Class.forName(fullClassName);
                            if (!clazz.isAnnotationPresent(MiniComponent.class))
                                continue;
                            Object instance = clazz.getDeclaredConstructor().newInstance();
                            String qualifier = null;
                            if (clazz.isAnnotationPresent(MiniQualifier.class)) {
                                qualifier = clazz.getAnnotation(MiniQualifier.class).value();
                            }
                            boolean isPrimary = clazz.isAnnotationPresent(MiniPrimary.class);
                            setBean(clazz, instance, qualifier, isPrimary);
                            for (Class<?> interfaceClass : clazz.getInterfaces()) {
                                setBean(interfaceClass, instance, qualifier, isPrimary);
                            }
                        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
                                 IllegalAccessException | InvocationTargetException e) {
                            System.err.println("Can't instantiate " + fullClassName);
                            System.err.println("Error type: " + e.getClass().getSimpleName());
                        }
                    }
                } else {
                    System.out.println("Skip protocol " + protocol);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("패키지를 찾을 수 없음: " + packagePath + " (scan fail)");
        }

        //bean 등록
        Method[] methods = configClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(MiniBean.class)) {
                try {
                    Object beanInstance = method.invoke(configInstance);
                    Class<?> beanClass = method.getReturnType();
                    String qualifier = null;
                    if (method.isAnnotationPresent(MiniQualifier.class)) {
                        qualifier = method.getAnnotation(MiniQualifier.class).value();
                    }
                    boolean isPrimary = method.isAnnotationPresent(MiniPrimary.class);
                    setBean(beanClass, beanInstance, qualifier, isPrimary);
                } catch (Exception e) {
                    System.err.println("Can't instantiate " + method.getName());
                    System.err.println(e.getMessage());
                }
            }
        }


        //autowired 주입
        for (List<BeanDefinition> beanList : beans.values()) {
            for (BeanDefinition beanDef : beanList) {
                Object bean = beanDef.getInstance();
                Class<?> beanClass = bean.getClass();
                Field[] fields = beanClass.getDeclaredFields();

                for (Field field : fields) {
                    if (field.isAnnotationPresent(MiniAutowired.class)) {
                        Class<?> fieldType = field.getType();
                        String requiredQualifier = null;
                        if (field.isAnnotationPresent(MiniQualifier.class)) {
                            requiredQualifier = field.getAnnotation(MiniQualifier.class).value();
                        }
                        
                        Object dependency = findBean(fieldType, requiredQualifier);
                        try {
                            if (dependency == null)
                                throw new NoSuchBeanException(fieldType.getName());
                            field.setAccessible(true);
                            field.set(bean, dependency);
                        } catch (IllegalAccessException e) {
                            System.err.println("Can't Set Dependency" + field.getName());
                            System.err.println(e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private void setBean(Class<?> type, Object instance, String qualifier, boolean isPrimary) {
        List<BeanDefinition> beanList = beans.computeIfAbsent(type, k -> new ArrayList<>());
        beanList.add(new BeanDefinition(instance, qualifier, isPrimary));
    }
    
    private Object findBean(Class<?> type, String qualifier) {
        List<BeanDefinition> beanList = beans.get(type);
        if (beanList == null || beanList.isEmpty()) {
            return null;
        }
        
        // 퀄리파이어가 null이면 모든 빈 반환, 아니면 퀄리파이어와 일치하는 빈만 필터링
        List<BeanDefinition> matchingBeans = (qualifier == null) 
            ? new ArrayList<>(beanList)
            : beanList.stream()
                .filter(beanDef -> beanDef.matchesQualifier(qualifier))
                .toList();
            
        if (matchingBeans.isEmpty()) {
            throw new NoSuchBeanException("No qualifying bean of type '" + type.getName() + 
                "'" + (qualifier != null ? " with qualifier '" + qualifier + "'" : "") + 
                " available");
        }
        
        // 매칭되는 빈이 여러 개면 primary=true인 빈 찾기
        if (matchingBeans.size() > 1) {
            List<BeanDefinition> primaryBeans = matchingBeans.stream()
                .filter(BeanDefinition::isPrimary)
                .toList();
                
            if (primaryBeans.size() == 1) {
                return primaryBeans.get(0).getInstance();
            } else if (primaryBeans.size() > 1) {
                throw new NoUniqueBeanException("Multiple primary beans found for type '" + type.getName() + 
                    "': " + primaryBeans.stream()
                        .map(b -> b.getInstance().getClass().getName())
                        .collect(java.util.stream.Collectors.joining(", ")));
            }
            
            // primary가 없는 경우 기존과 동일하게 예외 발생
            throw new NoUniqueBeanException("No qualifying bean of type '" + type.getName() + 
                "'" + (qualifier != null ? " with qualifier '" + qualifier + "'" : "") + 
                " available: expected single matching bean but found " + matchingBeans.size() + 
                ": " + matchingBeans.stream()
                    .map(b -> b.getInstance().getClass().getName() + 
                         (b.getQualifier() != null ? "@'" + b.getQualifier() + "'" : ""))
                    .collect(java.util.stream.Collectors.joining(", ")));
        }
        
        return matchingBeans.get(0).getInstance();
    }

    public <T> T getBean(Class<T> type) {
        return getBean(type, null);
    }

    public <T> T getBean(Class<T> type, String qualifier) {
        return (T) findBean(type, qualifier);
    }
}
