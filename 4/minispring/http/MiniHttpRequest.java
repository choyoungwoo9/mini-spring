package minispring.http;

import java.util.HashMap;
import java.util.Map;

public class MiniHttpRequest {
    private final String method;
    private final String path;
    private final Map<String, String> queryParams;
    private final Map<String, String> headers;

    private MiniHttpRequest(String method, String path) {
        this.method = method;
        this.path = path;
        this.queryParams = new HashMap<>();
        this.headers = new HashMap<>();
    }

    public static MiniHttpRequest get(String path) {
        return new MiniHttpRequest("GET", path);
    }

    public static MiniHttpRequest post(String path) {
        return new MiniHttpRequest("POST", path);
    }

    public MiniHttpRequest query(String name, String value) {
        this.queryParams.put(name, value);
        return this;
    }

    public MiniHttpRequest header(String name, String value) {
        this.headers.put(name, value);
        return this;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public Map<String, String> getQueryParams() {
        return new HashMap<>(queryParams);
    }

    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }
}