package constructor_injection_app;

import minispring.annotation.MiniComponent;

@MiniComponent
public class UserRepository {
    public String findUserById(String id) {
        return "User " + id;
    }
}
