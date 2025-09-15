package minispring.mvc;

import minispring.http.MiniHttpResponse;

import java.util.Collection;
import java.util.Map;

public class JsonReturnValueHandler implements HandlerMethodReturnValueHandler {
    
    @Override
    public boolean supports(Object returnValue) {
        return returnValue != null;
    }
    
    @Override
    public void handle(Object returnValue, MiniHttpResponse response) {
        String json = toJson(returnValue);
        response.setJsonBody(json);
    }
    
    private String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        if (obj instanceof String) {
            return "\"" + escapeJson((String) obj) + "\"";
        }
        
        if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        
        if (obj instanceof Collection) {
            Collection<?> collection = (Collection<?>) obj;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (Object item : collection) {
                if (!first) sb.append(",");
                sb.append(toJson(item));
                first = false;
            }
            sb.append("]");
            return sb.toString();
        }
        
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                sb.append(toJson(entry.getKey().toString())).append(":").append(toJson(entry.getValue()));
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        
        return toJsonObject(obj);
    }
    
    private String toJsonObject(Object obj) {
        StringBuilder sb = new StringBuilder("{");
        Class<?> clazz = obj.getClass();
        
        if (clazz.isRecord()) {
            java.lang.reflect.RecordComponent[] components = clazz.getRecordComponents();
            boolean first = true;
            for (var component : components) {
                if (!first) sb.append(",");
                try {
                    Object value = component.getAccessor().invoke(obj);
                    sb.append("\"").append(component.getName()).append("\":").append(toJson(value));
                    first = false;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to serialize record", e);
                }
            }
        } else {
            java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
            boolean first = true;
            for (var field : fields) {
                field.setAccessible(true);
                if (!first) sb.append(",");
                try {
                    Object value = field.get(obj);
                    sb.append("\"").append(field.getName()).append("\":").append(toJson(value));
                    first = false;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to serialize field", e);
                }
            }
        }
        
        sb.append("}");
        return sb.toString();
    }
    
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}