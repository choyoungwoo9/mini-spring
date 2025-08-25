package simple_qualifier_app;

import minispring.annotation.MiniComponent;
import minispring.annotation.MiniQualifier;

@MiniComponent 
@MiniQualifier("welcome")
public class WelcomeMessageService implements MessageService {
    public String getMessage() { 
        return "Welcome! Hello World!"; 
    }
} 