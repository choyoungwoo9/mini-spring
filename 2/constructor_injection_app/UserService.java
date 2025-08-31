package constructor_injection_app;

import minispring.annotation.MiniAutowired;
import minispring.annotation.MiniComponent;

@MiniComponent
public class UserService {
    private final UserRepository userRepository;
    
    @MiniAutowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public String getUserInfo(String id) {
        return "User Info: " + userRepository.findUserById(id);
    }
}
