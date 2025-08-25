package com.example.springapp;

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Primary;

@Component
@Primary
public class SimpleFormatter implements Formatter {
    
    @Override
    public String format(String raw) {
        return raw;
    }
} 