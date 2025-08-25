package com.example.springapp;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import java.time.Clock;
import java.time.Instant;

@Component
public class MemoService {
    
    private final Formatter formatter;

    @Autowired
    public MemoService(@Qualifier("markdown") Formatter formatter) {
        this.formatter = formatter;
    }

    public String write(String content) {
        return formatter.format(content);
    }
} 