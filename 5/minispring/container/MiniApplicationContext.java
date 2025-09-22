package minispring.container;

import minispring.annotation.*;
import minispring.annotation.MiniController;
import minispring.aop.AopProxy;
import minispring.aop.PointcutMatcher;
import minispring.exception.NoSuchBeanException;
import minispring.task.MiniThreadPoolTaskExecutor;
import minispring.task.TaskExecutor;
import minispring.exception.NoUniqueBeanException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MiniApplicationContext {
    private final Map<Class<?>, List<BeanDefinition>> beans = new HashMap<>();
    private Object configInstance = null;
    private final Set<Class<?>> creatingBeans = new HashSet<>();
    private final Map<Class<?>, Object> singletonObjects = new HashMap<>();
    private final List<AspectInfo> aspects = new ArrayList<>();
    private TaskExecutor taskExecutor;
    private final Map<Class<?>, List<Method>> asyncMethodCache = new HashMap<>();

    public MiniApplicationContext(Class<?> configClass) {
        validateConfigClass(configClass);
        initializeConfigInstance(configClass);
        registerInternalBeans();
        scanAndRegisterComponents(configClass);
        registerBeansFromConfigMethods(configClass);
        injectDependencies();
        applyAsync();
        applyAop();
    }

    private void validateConfigClass(Class<?> configClass) {
        if (!configClass.isAnnotationPresent(MiniConfiguration.class)) {
            throw new RuntimeException("@MiniConfiguration annotation not found");
        }
    }

    private void initializeConfigInstance(Class<?> configClass) {
        try {
            configInstance = configClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create config instance", e);
        }
    }

    private void registerInternalBeans() {
        taskExecutor = new MiniThreadPoolTaskExecutor();
        singletonObjects.put(MiniThreadPoolTaskExecutor.class, taskExecutor);
        setBean(TaskExecutor.class, taskExecutor, null, true, MiniThreadPoolTaskExecutor.class);
        setBean(MiniThreadPoolTaskExecutor.class, taskExecutor, null, true, MiniThreadPoolTaskExecutor.class);
    }

    private void scanAndRegisterComponents(Class<?> configClass) {
        String packagePath = getPackagePath(configClass);
        
        try {
            List<URL> urls = Collections.list(Thread.currentThread().getContextClassLoader().getResources(packagePath));
            for (URL url : urls) {
                if ("file".equals(url.getProtocol())) {
                    processClassFilesInDirectory(url.getFile(), packagePath);
                } else {
                    logSkippedProtocol(url.getProtocol());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan package: " + packagePath, e);
        }
    }

    private String getPackagePath(Class<?> configClass) {
        MiniComponentScan scanAnnotation = configClass.getAnnotation(MiniComponentScan.class);
        if (scanAnnotation == null) {
            throw new RuntimeException("@MiniComponentScan annotation not found");
        }
        return scanAnnotation.value().replace('.', '/');
    }

    private void processClassFilesInDirectory(String directoryPath, String packagePath) {
        File directory = new File(directoryPath);
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".class")) {
                processClassFile(file, packagePath);
            }
        }
    }

    private void processClassFile(File file, String packagePath) {
        String className = file.getName().replace(".class", "");
        String fullClassName = packagePath + "/" + className;
        
        try {
            Class<?> clazz = Class.forName(fullClassName.replace('/', '.'));
            if (clazz.isAnnotationPresent(MiniComponent.class) || 
                clazz.isAnnotationPresent(MiniAspect.class) ||
                clazz.isAnnotationPresent(MiniController.class)) {
                registerComponentClass(clazz);
            }
        } catch (Exception e) {
            logClassLoadingError(fullClassName, e);
        }
    }

    private void registerComponentClass(Class<?> clazz) throws Exception {
        Object instance = createBean(clazz);
        String qualifier = extractQualifier(clazz);
        boolean isPrimary = clazz.isAnnotationPresent(MiniPrimary.class);
        
        registerBean(clazz, instance, qualifier, isPrimary);
        registerInterfaces(clazz, instance, qualifier, isPrimary);
        
        if (clazz.isAnnotationPresent(MiniAspect.class)) {
            registerAspect(instance, clazz);
        }
    }

    // 생성 중인 빈을 추적하기 위한 컬렉션
    
    private Object createBean(Class<?> clazz) throws Exception {
        // 이미 생성된 빈이 있으면 반환
        if (singletonObjects.containsKey(clazz)) {
            return singletonObjects.get(clazz);
        }
        
        // 순환 의존성 체크
        if (creatingBeans.contains(clazz)) {
            throw new RuntimeException("Circular dependency detected while creating bean of type: " + clazz.getName());
        }
        
        creatingBeans.add(clazz);
        
        try {
            // 생성자 선택
            Constructor<?> constructorToUse = null;
            Constructor<?>[] candidates = clazz.getConstructors();
            
            // @MiniAutowired가 있는 생성자 찾기
            for (Constructor<?> candidate : candidates) {
                if (candidate.isAnnotationPresent(MiniAutowired.class)) {
                    if (constructorToUse != null) {
                        throw new RuntimeException("Multiple @MiniAutowired constructors found in " + clazz.getName());
                    }
                    constructorToUse = candidate;
                }
            }
            
            // @MiniAutowired가 없고 생성자가 하나뿐이면 그 생성자 사용
            if (constructorToUse == null && candidates.length == 1) {
                constructorToUse = candidates[0];
            }
            
            // 인스턴스 생성
            Object instance;
            if (constructorToUse != null && constructorToUse.getParameterCount() > 0) {
                // 매개변수가 있는 생성자 처리
                Class<?>[] paramTypes = constructorToUse.getParameterTypes();
                Object[] args = new Object[paramTypes.length];
                
                for (int i = 0; i < paramTypes.length; i++) {
                    // 의존성 주입 시도
                    try {
                        // 빈을 찾아보고 없으면 재귀적으로 생성
                        args[i] = findBean(paramTypes[i], null);
                        if (args[i] == null) {
                            // 빈을 찾을 수 없으면 새로 생성 시도
                            args[i] = createBean(paramTypes[i]);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to create bean of type '" + 
                                paramTypes[i].getName() + "' for constructor parameter " + i + 
                                " in " + clazz.getName(), e);
                    }
                }
                instance = constructorToUse.newInstance(args);
            } else {
                // 기본 생성자 사용
                instance = clazz.getDeclaredConstructor().newInstance();
            }
            
            // 생성된 인스턴스 저장 (싱글톤)
            singletonObjects.put(clazz, instance);
            return instance;
            
        } finally {
            creatingBeans.remove(clazz);
        }
    }

    private String extractQualifier(Class<?> clazz) {
        return clazz.isAnnotationPresent(MiniQualifier.class) 
            ? clazz.getAnnotation(MiniQualifier.class).value() 
            : null;
    }

    private void registerBean(Class<?> type, Object instance, String qualifier, boolean isPrimary) {
        setBean(type, instance, qualifier, isPrimary, type);
    }

    private void registerInterfaces(Class<?> clazz, Object instance, String qualifier, boolean isPrimary) {
        for (Class<?> iface : clazz.getInterfaces()) {
            setBean(iface, instance, qualifier, isPrimary, clazz);
        }
    }

    private void logClassLoadingError(String className, Exception e) {
        System.err.println("Failed to load class: " + className);
        System.err.println("Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
    }

    private void logSkippedProtocol(String protocol) {
        System.out.println("Skipping unsupported protocol: " + protocol);
    }

    private void registerBeansFromConfigMethods(Class<?> configClass) {
        for (Method method : configClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(MiniBean.class)) {
                processBeanMethod(method);
            }
        }
    }

    private void processBeanMethod(Method method) {
        try {
            Object beanInstance = method.invoke(configInstance);
            Class<?> beanClass = method.getReturnType();
            String qualifier = extractQualifier(method);
            boolean isPrimary = method.isAnnotationPresent(MiniPrimary.class);
            
            setBean(beanClass, beanInstance, qualifier, isPrimary, beanClass);
        } catch (Exception e) {
            logBeanCreationError(method.getName(), e);
        }
    }

    private String extractQualifier(Method method) {
        return method.isAnnotationPresent(MiniQualifier.class)
            ? method.getAnnotation(MiniQualifier.class).value()
            : null;
    }

    private void logBeanCreationError(String methodName, Exception e) {
        System.err.println("Failed to create bean from method: " + methodName);
        System.err.println("Error: " + e.getMessage());
    }

    private void injectDependencies() {
        beans.values().stream()
            .flatMap(List::stream)
            .forEach(this::injectDependenciesIntoBean);
    }

    private void injectDependenciesIntoBean(BeanDefinition beanDef) {
        Object bean = beanDef.getInstance();
        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(MiniAutowired.class)) {
                injectDependency(bean, field);
            }
        }
    }

    private void injectDependency(Object bean, Field field) {
        try {
            Class<?> fieldType = field.getType();
            String qualifier = extractQualifier(field);
            
            Object dependency = findBean(fieldType, qualifier);
            if (dependency == null) {
                throw new NoSuchBeanException(fieldType.getName());
            }
            
            field.setAccessible(true);
            field.set(bean, dependency);
        } catch (IllegalAccessException e) {
            logDependencyInjectionError(bean, field, e);
        }
    }

    private String extractQualifier(Field field) {
        return field.isAnnotationPresent(MiniQualifier.class)
            ? field.getAnnotation(MiniQualifier.class).value()
            : null;
    }

    private void logDependencyInjectionError(Object bean, Field field, Exception e) {
        System.err.println("Failed to inject dependency into " + bean.getClass().getSimpleName() + "." + field.getName());
        System.err.println("Error: " + e.getMessage());
    }

    private void setBean(Class<?> type, Object instance, String qualifier, boolean isPrimary, Class<?> beanClass) {
        List<BeanDefinition> beanList = beans.computeIfAbsent(type, k -> new ArrayList<>());
        beanList.add(new BeanDefinition(instance, qualifier, isPrimary, beanClass));
    }
    
    private Object findBean(Class<?> type, String qualifier) {
        List<BeanDefinition> beanList = beans.get(type);
        if (beanList == null || beanList.isEmpty()) {
            return null;
        }
        
        List<BeanDefinition> matchingBeans = findMatchingBeans(beanList, qualifier);
        
        if (matchingBeans.isEmpty()) {
            throw new NoSuchBeanException(createNoSuchBeanMessage(type, qualifier));
        }
        
        if (matchingBeans.size() > 1) {
            return handleMultipleMatchingBeans(type, qualifier, matchingBeans);
        }
        
        return matchingBeans.get(0).getInstance();
    }
    
    private List<BeanDefinition> findMatchingBeans(List<BeanDefinition> beanList, String qualifier) {
        return (qualifier == null) 
            ? new ArrayList<>(beanList)
            : beanList.stream()
                .filter(beanDef -> beanDef.matchesQualifier(qualifier))
                .collect(Collectors.toList());
    }
    
    private Object handleMultipleMatchingBeans(Class<?> type, String qualifier, List<BeanDefinition> matchingBeans) {
        List<BeanDefinition> primaryBeans = findPrimaryBeans(matchingBeans);
        
        if (primaryBeans.size() == 1) {
            return primaryBeans.get(0).getInstance();
        }
        
        if (primaryBeans.size() > 1) {
            throw new NoUniqueBeanException(createMultiplePrimaryBeansMessage(type, primaryBeans));
        }
        
        throw new NoUniqueBeanException(createNoUniqueBeanMessage(type, qualifier, matchingBeans));
    }
    
    private List<BeanDefinition> findPrimaryBeans(List<BeanDefinition> beans) {
        return beans.stream()
            .filter(BeanDefinition::isPrimary)
            .collect(Collectors.toList());
    }
    
    private String createNoSuchBeanMessage(Class<?> type, String qualifier) {
        return String.format("No qualifying bean of type '%s'%s available",
            type.getName(),
            qualifier != null ? " with qualifier '" + qualifier + "'" : "");
    }
    
    private String createMultiplePrimaryBeansMessage(Class<?> type, List<BeanDefinition> primaryBeans) {
        String beanNames = primaryBeans.stream()
            .map(b -> b.getInstance().getClass().getName())
            .collect(Collectors.joining(", "));
            
        return String.format("Multiple primary beans found for type '%s': %s", type.getName(), beanNames);
    }
    
    private String createNoUniqueBeanMessage(Class<?> type, String qualifier, List<BeanDefinition> matchingBeans) {
        String beanNames = matchingBeans.stream()
            .map(b -> {
                String name = b.getInstance().getClass().getName();
                return b.getQualifier() != null ? name + "@'" + b.getQualifier() + "'" : name;
            })
            .collect(Collectors.joining(", "));
            
        return String.format("No qualifying bean of type '%s'%s available: expected single matching bean but found %d: %s",
            type.getName(),
            qualifier != null ? " with qualifier '" + qualifier + "'" : "",
            matchingBeans.size(),
            beanNames);
    }

    public <T> T getBean(Class<T> type) {
        return getBean(type, null);
    }

    public <T> T getBean(Class<T> type, String qualifier) {
        return (T) findBean(type, qualifier);
    }

    public Collection<Object> getAllBeans() {
        return beans.values().stream()
                .flatMap(List::stream)
                .map(BeanDefinition::getInstance)
                .collect(Collectors.toList());
    }

    private void registerAspect(Object instance, Class<?> clazz) {
        AspectInfo aspectInfo = new AspectInfo(instance);
        
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(MiniBefore.class)) {
                String pointcut = method.getAnnotation(MiniBefore.class).value();
                aspectInfo.addBeforeAdvice(method, pointcut);
            }
            if (method.isAnnotationPresent(MiniAfter.class)) {
                String pointcut = method.getAnnotation(MiniAfter.class).value();
                aspectInfo.addAfterAdvice(method, pointcut);
            }
        }
        
        aspects.add(aspectInfo);
    }

    public List<AspectInfo> getAspects() {
        return aspects;
    }

    private void applyAsync() {
        if (taskExecutor == null) {
            return;
        }

        List<BeanDefinition> definitions = beans.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());

        Set<Object> processed = new HashSet<>();
        for (BeanDefinition beanDef : definitions) {
            Object current = beanDef.getInstance();
            if (!processed.add(current)) {
                continue;
            }
            Class<?> beanClass = beanDef.getBeanClass();
            Object proxy = createAsyncProxyIfNeeded(current, beanClass);
            if (proxy != null) {
                replaceBeanInstances(current, proxy, beanClass);
                processed.add(proxy);
            }
        }
    }

    private Object createAsyncProxyIfNeeded(Object target, Class<?> beanClass) {
        List<Method> asyncMethods = asyncMethodCache.computeIfAbsent(beanClass, this::findAsyncMethods);
        if (asyncMethods.isEmpty()) {
            return null;
        }

        Class<?>[] interfaces = beanClass.getInterfaces();
        if (interfaces.length == 0) {
            return null;
        }

        Set<MethodKey> asyncMethodKeys = asyncMethods.stream()
            .map(MethodKey::new)
            .collect(Collectors.toSet());

        return Proxy.newProxyInstance(
            beanClass.getClassLoader(),
            interfaces,
            new AsyncInvocationHandler(target, asyncMethodKeys, taskExecutor)
        );
    }

    private List<Method> findAsyncMethods(Class<?> beanClass) {
        List<Method> result = new ArrayList<>();
        for (Method method : beanClass.getMethods()) {
            if (method.isAnnotationPresent(MiniAsync.class)) {
                result.add(method);
            }
        }
        return result;
    }

    private void replaceBeanInstances(Object original, Object proxy, Class<?> beanClass) {
        for (List<BeanDefinition> beanList : beans.values()) {
            for (BeanDefinition beanDef : beanList) {
                if (beanDef.getInstance() == original) {
                    beanDef.setInstance(proxy);
                }
            }
        }
        singletonObjects.put(beanClass, proxy);
    }

    private void applyAop() {
        if (aspects.isEmpty()) {
            return;
        }
        
        for (Map.Entry<Class<?>, List<BeanDefinition>> entry : beans.entrySet()) {
            List<BeanDefinition> beanList = entry.getValue();
            
            for (BeanDefinition beanDef : beanList) {
                Object bean = beanDef.getInstance();
                if (needsProxy(bean)) {
                    Object proxy = AopProxy.createProxy(bean, aspects);
                    beanDef.setInstance(proxy);
                }
            }
        }
    }
    
    private boolean needsProxy(Object bean) {
        if (bean.getClass().getInterfaces().length == 0) {
            return false;
        }
        
        for (Method method : bean.getClass().getMethods()) {
            for (AspectInfo aspect : aspects) {
                for (AdviceInfo advice : aspect.getBeforeAdvices()) {
                    if (advice.matches(method, bean.getClass())) {
                        return true;
                    }
                }
                for (AdviceInfo advice : aspect.getAfterAdvices()) {
                    if (advice.matches(method, bean.getClass())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static Object invokeAndUnwrap(Method method, Object target, Object[] args) {
        try {
            return method.invoke(target, prepareArgs(args));
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException(cause);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object[] prepareArgs(Object[] args) {
        return args == null ? new Object[0] : args;
    }

    private static final class MethodKey {
        private final String name;
        private final Class<?>[] parameterTypes;

        MethodKey(Method method) {
            this(method.getName(), method.getParameterTypes());
        }

        MethodKey(String name, Class<?>[] parameterTypes) {
            this.name = name;
            this.parameterTypes = parameterTypes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MethodKey)) return false;
            MethodKey that = (MethodKey) o;
            return Objects.equals(name, that.name) && Arrays.equals(parameterTypes, that.parameterTypes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, Arrays.hashCode(parameterTypes));
        }
    }

    private static class AsyncInvocationHandler implements InvocationHandler {
        private final Object target;
        private final Set<MethodKey> asyncMethods;
        private final TaskExecutor executor;

        AsyncInvocationHandler(Object target, Set<MethodKey> asyncMethods, TaskExecutor executor) {
            this.target = target;
            this.asyncMethods = asyncMethods;
            this.executor = executor;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(target, prepareArgs(args));
            }

            MethodKey key = new MethodKey(method.getName(), method.getParameterTypes());
            if (asyncMethods.contains(key)) {
                Object[] capturedArgs = args == null ? null : Arrays.copyOf(args, args.length);
                executor.execute(() -> invokeAndUnwrap(method, target, capturedArgs));
                return defaultReturnValue(method.getReturnType());
            }
            return invokeAndUnwrap(method, target, args);
        }

        private Object defaultReturnValue(Class<?> returnType) {
            if (returnType == void.class || returnType == Void.class) {
                return null;
            }
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == char.class) {
                return '\0';
            }
            if (returnType == byte.class) {
                return (byte) 0;
            }
            if (returnType == short.class) {
                return (short) 0;
            }
            if (returnType == int.class) {
                return 0;
            }
            if (returnType == long.class) {
                return 0L;
            }
            if (returnType == float.class) {
                return 0f;
            }
            if (returnType == double.class) {
                return 0d;
            }
            return null;
        }
    }

    public static class AspectInfo {
        private final Object aspectInstance;
        private final List<AdviceInfo> beforeAdvices = new ArrayList<>();
        private final List<AdviceInfo> afterAdvices = new ArrayList<>();
        
        public AspectInfo(Object aspectInstance) {
            this.aspectInstance = aspectInstance;
        }
        
        public Object getAspectInstance() {
            return aspectInstance;
        }
        
        public List<AdviceInfo> getBeforeAdvices() {
            return beforeAdvices;
        }
        
        public List<AdviceInfo> getAfterAdvices() {
            return afterAdvices;
        }
        
        public void addBeforeAdvice(Method method, String pointcut) {
            beforeAdvices.add(new AdviceInfo(method, pointcut));
        }
        
        public void addAfterAdvice(Method method, String pointcut) {
            afterAdvices.add(new AdviceInfo(method, pointcut));
        }
    }
    
    public static class AdviceInfo {
        private final Method method;
        private final String pointcut;
        
        public AdviceInfo(Method method, String pointcut) {
            this.method = method;
            this.pointcut = pointcut;
        }
        
        public Method getMethod() {
            return method;
        }
        
        public String getPointcut() {
            return pointcut;
        }
        
        public boolean matches(Method targetMethod, Class<?> targetClass) {
            return PointcutMatcher.matches(pointcut, targetMethod, targetClass);
        }
    }
}
