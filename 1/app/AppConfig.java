package app;

import minispring.annotation.MiniBean;
import minispring.annotation.MiniComponentScan;
import minispring.annotation.MiniConfiguration;

import java.time.Clock;

@MiniConfiguration
@MiniComponentScan("app")
public class AppConfig {
    @MiniBean
    public Clock clock() { return Clock.systemUTC(); }
}