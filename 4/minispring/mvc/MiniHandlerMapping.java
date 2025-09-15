package minispring.mvc;

import minispring.http.MiniHttpRequest;

public interface MiniHandlerMapping {
    HandlerExecutionChain getHandler(MiniHttpRequest request);
}