package simple_qualifier_app;

import minispring.annotation.MiniComponent;

@MiniComponent
public class DefaultMessageService implements MessageService {
    public String getMessage() { 
        return "Default Message"; 
    }
} 