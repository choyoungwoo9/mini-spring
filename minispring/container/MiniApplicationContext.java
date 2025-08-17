package minispring.container;

import minispring.annotation.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class MiniApplicationContext {
    private final Map<Class<?>, Object> beans = new HashMap<>();
    private Object configInstance = null;

    public MiniApplicationContext(Class<?> configClass) {
        if(!configClass.isAnnotationPresent(MiniConfiguration.class)) {
            //TODO: 전용 에러 클래스로 변경
            throw new RuntimeException("@MiniConfiguration annotation not found");
        }

        try {
            configInstance = configClass.getDeclaredConstructor().newInstance();
        } catch(Exception e) {
            throw new RuntimeException("Failed to create config instance", e);
        }

        MiniComponentScan msc = configClass.getAnnotation(MiniComponentScan.class);
        if(msc == null) {
            //에러 대신 config 클래스 하위 경로를 사용할지 고민
            throw new RuntimeException("@MiniComponentScan annotation not found");
        }

        String packagePath = msc.value().replace('.', '/');
        if(Thread.currentThread().getContextClassLoader().getResource(packagePath) == null) {
            throw new RuntimeException("Package not found: " + packagePath + " (scan fail)");
        }

        //컴포넌트 스캔 후 컴포넌트 등록
        try {
            List<URL> urls = Collections.list(Thread.currentThread().getContextClassLoader().getResources(packagePath));
            for(URL url : urls) {
                String protocol = url.getProtocol();
                if(protocol.equals("file")) {
                    File directory = new File(url.getFile());
                    File[] files = directory.listFiles();
                    for(File file : files) {
                        if(!file.getName().endsWith(".class"))
                            continue;
                        String className = file.getName().substring(0, file.getName().length() - 6);
                        String fullClassName = packagePath + "." + className;
                        try {
                            Class<?> clazz = Class.forName(fullClassName);
                            if(!clazz.isAnnotationPresent(MiniComponent.class))
                                continue;
                            Object instance = clazz.getDeclaredConstructor().newInstance();
                            beans.put(clazz, instance);
                        } catch (Exception e) {
                            System.err.println("Can't instantiate " + fullClassName);
                            System.err.println(e.getMessage());
                        }

                    }
                } else {
                    System.out.println("Skip protocol " + protocol);
                }
            }
        } catch(IOException e) {
            throw new RuntimeException("패키지를 찾을 수 없음: " + packagePath + " (scan fail)");
        }

        //bean 등록
        Method[] methods = configClass.getDeclaredMethods();
        for(Method method : methods) {
            if(method.isAnnotationPresent(MiniBean.class)) {
                try {
                    Object beanInstance = method.invoke(configInstance);
                    Class<?> beanClass = method.getReturnType();
                    beans.put(beanClass, beanInstance);
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

            for(Field field : fields) {
                if(field.isAnnotationPresent(MiniAutowired.class)) {
                    Class<?> fieldType = field.getType();
                    Object dependency = beans.get(fieldType);
                    try {
                        if(dependency != null) {
                            field.setAccessible(true);
                            field.set(bean, dependency);
                        }
                    } catch (Exception e) {
                        System.err.println("Can't Set Dependency" + field.getName());
                        System.err.println(e.getMessage());
                    }

                }
            }
        }
    }

    public <T> T getBean(Class<T> type){
        Object bean = beans.get(type);
        if(bean == null)
            throw new RuntimeException("Bean not found: " + type);
        return type.cast(bean);
    }
}
