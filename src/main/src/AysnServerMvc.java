import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author TX
 * @date 2022/5/6 11:42
 */
@WebServlet(name = "s2",
        urlPatterns = "/as2",
        asyncSupported = true
)
public class AysnServerMvc extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("请求类型：" + request.getDispatcherType());
        //@1：判断请求类型，如果是异步类型（DispatcherType.ASYNC），则说明是异步转发过来的，将结果输出
        if (request.getDispatcherType() == DispatcherType.ASYNC) {
            System.out.println("响应结果：" + Thread.currentThread() + "-" + System.currentTimeMillis() + "-start");
            //从request中获取结果，然后输出
            Object result = request.getAttribute("result");
            response.getWriter().write(result.toString());
            System.out.println("响应结果：" + Thread.currentThread() + "-" + System.currentTimeMillis() + "-end");
        } else {
            long st = System.currentTimeMillis();
            System.out.println("主线程：" + Thread.currentThread() + "-" + System.currentTimeMillis() + "-start");
            //2、启动异步处理：调用req.startAsync(request,response)方法，获取异步处理上下文对象AsyncContext
            final AsyncContext asyncContext = request.startAsync(request, response);
            //3、调用start方法异步处理，调用这个方法之后主线程就结束了
            asyncContext.start(new Runnable() {
                @Override
                public void run() {
                    long stSon = System.currentTimeMillis();
                    System.out.println("子线程：" + Thread.currentThread() + "-" + System.currentTimeMillis() + "-start");
                    try {
                        //这里休眠2秒，模拟业务耗时
                        TimeUnit.SECONDS.sleep(2);
                        //将结果丢到request中
                        asyncContext.getRequest().setAttribute("result", "ok");
                        //转发请求，调用这个方法之后，请求又会被转发到当前的servlet，又会进入当前servlet的service方法
                        //此时请求的类型（request.getDispatcherType()）是DispatcherType.ASYNC，所以通过这个值可以判断请求是异步转发过来的
                        //然后在request中将结果取出，对应代码@1，然后输出
                        asyncContext.dispatch();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("子线程：" + Thread.currentThread() + "-" + System.currentTimeMillis() + "-end,耗时(ms):" + (System.currentTimeMillis() - stSon));
                }
            });
            System.out.println("主线程：" + Thread.currentThread() + "-" + System.currentTimeMillis() + "-end,耗时(ms):" + (System.currentTimeMillis() - st));
        }
    }

}

