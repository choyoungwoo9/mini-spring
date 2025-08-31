package primary_app;

import minispring.annotation.MiniComponent;

@MiniComponent
public class DefaultPrimaryService implements PrimaryService {
    @Override
    public String serve() {
        return "Default Service";
    }
}
