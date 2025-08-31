package multi_dependency_app;

import minispring.annotation.MiniComponent;

@MiniComponent
public class AService {
    public String getMessage() {
        return "Hello from AService";
    }
}
