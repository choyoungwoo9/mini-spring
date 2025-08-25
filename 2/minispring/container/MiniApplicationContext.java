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
    private final Map<Class<?>, Object> beans = new HashMap<>();
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
                            setBean(clazz, instance);
                            for (Class<?> interfaceClass : clazz.getInterfaces()) {
                                setBean(interfaceClass, instance);
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
                    setBean(beanClass, beanInstance);
                } catch (Exception e) {
                    System.err.println("Can't instantiate " + method.getName());
                    System.err.println(e.getMessage());
                }
            }
        }


        //autowired 주입
        for (Object bean : beans.values()) {
            Class<?> beanClass = bean.getClass();
            Field[] fields = beanClass.getDeclaredFields();

            for (Field field : fields) {
                if (field.isAnnotationPresent(MiniAutowired.class)) {
                    Class<?> fieldType = field.getType();
                    Object dependency = beans.get(fieldType);
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

    private void setBean(Class<?> type, Object instance) {
        if (beans.containsKey(type)) {
            throw new NoUniqueBeanException("Multiple beans found for type: " + type.getName());
        }
        beans.put(type, instance);
    }

    public <T> T getBean(Class<T> type) {
        Object bean = beans.get(type);
        if (bean == null)
            throw new NoSuchBeanException("Bean not found: " + type.getName());
        return type.cast(bean);
    }
}
