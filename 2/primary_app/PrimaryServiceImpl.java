package primary_app;

import minispring.annotation.MiniComponent;
import minispring.annotation.MiniPrimary;

@MiniComponent
@MiniPrimary
public class PrimaryServiceImpl implements PrimaryService {
    @Override
    public String serve() {
        return "Primary Service";
    }
}
