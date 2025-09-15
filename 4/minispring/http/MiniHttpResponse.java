package minispring.http;

import java.util.HashMap;
import java.util.Map;

public class MiniHttpResponse {
    private int status = 200;
    private final Map<String, String> headers = new HashMap<>();
    private String body = "";

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public Map<String, String> getHeaders() {
        return new HashMap<>(headers);
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBodyAsString() {
        return body;
    }

    public void setJsonBody(String json) {
        setHeader("Content-Type", "application/json; charset=utf-8");
        setBody(json);
    }

    public void setErrorJson(int status, String message) {
        setStatus(status);
        setJsonBody("{\"error\":\"" + message + "\"}");
    }
}