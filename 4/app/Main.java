package app;

import minispring.container.MiniApplicationContext;
import minispring.mvc.DispatcherServlet;
import minispring.http.MiniHttpRequest;
import minispring.http.MiniHttpResponse;

public class Main {
    public static void main(String[] args) {
        var ctx = new MiniApplicationContext(WebApp.class);
        var dispatcher = new DispatcherServlet(ctx);

        var req = MiniHttpRequest.get("/memos/new").query("content", "hi");
        var res = new MiniHttpResponse();
        dispatcher.service(req, res);

        System.out.println(res.getStatus());        // 200
        System.out.println(res.getBodyAsString());  // {"content":"[...timestamp...] hi"}
    }
}