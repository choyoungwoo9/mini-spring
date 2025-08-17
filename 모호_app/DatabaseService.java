package 모호_app;

import minispring.annotation.MiniComponent;

@MiniComponent
public class DatabaseService implements DataService {
    @Override
    public void processData() {
        System.out.println("Processing data with Database");
    }
}