package multi_dependency_app;

import minispring.annotation.MiniComponent;

@MiniComponent
public class CService {
    public String getMessage() {
        return "Hello from CService";
    }
}
