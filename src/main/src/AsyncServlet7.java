/**
 * @author TX
 * @date 2022/5/6 14:38
 */
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

//1.设置@WebServlet的asyncSupported属性为true，表示支持异步处理
@WebServlet(name = "AsyncServlet7",
        urlPatterns = "/as7",
        asyncSupported = true
)
public class AsyncServlet7 extends HttpServlet {
    Map<String, AsyncContext> orderIdAsyncContextMap = new ConcurrentHashMap();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String orderId = request.getParameter("orderId");
        String result = request.getParameter("result");
        AsyncContext async;
        if (orderId != null && result != null && (async = orderIdAsyncContextMap.get(orderId)) != null) {
            async.getResponse().getWriter().write(String.format("<br/>" +
                    "%s:%s:result:%s", Thread.currentThread(), System.currentTimeMillis(), result));
            async.complete();
        } else {
            AsyncContext asyncContext = request.startAsync(request, response);
            orderIdAsyncContextMap.put("1", asyncContext);
            asyncContext.getResponse().setContentType("text/html;charset=utf-8");
            asyncContext.getResponse().getWriter().write(String.format("%s:%s:%s", Thread.currentThread(), System.currentTimeMillis(), "start"));
        }
    }
}
