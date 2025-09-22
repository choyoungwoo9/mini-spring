package minispring.mvc;

import minispring.container.MiniApplicationContext;
import minispring.http.MiniHttpRequest;
import minispring.http.MiniHttpResponse;

import java.util.ArrayList;
import java.util.List;

public class DispatcherServlet {
    private final List<MiniHandlerMapping> handlerMappings = new ArrayList<>();
    private final List<MiniHandlerAdapter> handlerAdapters = new ArrayList<>();
    
    public DispatcherServlet(MiniApplicationContext context) {
        initHandlerMappings(context);
        initHandlerAdapters();
    }
    
    private void initHandlerMappings(MiniApplicationContext context) {
        handlerMappings.add(new RequestMappingHandlerMapping(context));
    }
    
    private void initHandlerAdapters() {
        handlerAdapters.add(new RequestMappingHandlerAdapter());
    }
    
    public void service(MiniHttpRequest request, MiniHttpResponse response) {
        try {
            HandlerExecutionChain handler = getHandler(request);
            if (handler == null) {
                response.setErrorJson(404, "Not Found");
                return;
            }
            
            MiniHandlerAdapter adapter = getHandlerAdapter(handler);
            if (adapter == null) {
                response.setErrorJson(500, "No adapter for handler");
                return;
            }
            
            adapter.handle(request, response, handler);
            
        } catch (IllegalArgumentException e) {
            response.setErrorJson(400, e.getMessage());
        } catch (Exception e) {
            response.setErrorJson(500, "Internal Server Error: " + e.getMessage());
        }
    }
    
    private HandlerExecutionChain getHandler(MiniHttpRequest request) {
        for (MiniHandlerMapping mapping : handlerMappings) {
            HandlerExecutionChain handler = mapping.getHandler(request);
            if (handler != null) {
                return handler;
            }
        }
        return null;
    }
    
    private MiniHandlerAdapter getHandlerAdapter(Object handler) {
        for (MiniHandlerAdapter adapter : handlerAdapters) {
            if (adapter.supports(handler)) {
                return adapter;
            }
        }
        return null;
    }
}