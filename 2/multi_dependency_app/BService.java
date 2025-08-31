package multi_dependency_app;

import minispring.annotation.MiniAutowired;
import minispring.annotation.MiniComponent;

@MiniComponent
public class BService {
    private final AService aService;
    private final CService cService;
    
    @MiniAutowired
    public BService(AService aService, CService cService) {
        this.aService = aService;
        this.cService = cService;
    }
    
    public String getMessage() {
        return String.format("BService depends on:\n- %s\n- %s", 
            aService.getMessage(), 
            cService.getMessage());
    }
}
