package 모호_app;

import minispring.annotation.MiniComponent;

@MiniComponent
public class FileService implements DataService {
    @Override
    public void processData() {
        System.out.println("Processing data with File");
    }
}