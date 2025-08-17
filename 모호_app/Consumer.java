package 모호_app;

import minispring.annotation.MiniAutowired;
import minispring.annotation.MiniComponent;

@MiniComponent
public class Consumer {
    @MiniAutowired
    private DataService dataService;  // 모호함! DatabaseService vs FileService

    public void doWork() {
        dataService.processData();
    }
}