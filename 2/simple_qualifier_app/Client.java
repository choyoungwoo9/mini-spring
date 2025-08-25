package simple_qualifier_app;

import minispring.annotation.MiniComponent;
import minispring.annotation.MiniAutowired;
import minispring.annotation.MiniQualifier;

@MiniComponent
public class Client {
    @MiniAutowired
    @MiniQualifier("welcome")
    private MessageService messageService;

    public void printMessage() {
        System.out.println("Client received: " + messageService.getMessage());
    }
} 