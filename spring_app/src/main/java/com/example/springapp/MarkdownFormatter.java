package com.example.springapp;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

@Component
@Qualifier("markdown")
public class MarkdownFormatter implements Formatter {
    
    @Override
    public String format(String raw) {
        return "**" + raw + "**";
    }
} 