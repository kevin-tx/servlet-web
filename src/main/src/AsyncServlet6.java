//import jakarta.servlet.*;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

//1.设置@WebServlet的asyncSupported属性为true，表示支持异步处理
@WebServlet(name = "AsyncServlet6",
        urlPatterns = "/as6",
        asyncSupported = true
)
public class AsyncServlet6 extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getDispatcherType() == DispatcherType.ASYNC) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write(request.getAttribute("result").toString());
        } else {
            long st = System.currentTimeMillis();
            System.out.println("主线程：" + Thread.currentThread() + "-" + System.currentTimeMillis() + "-start");
            //2、启动异步处理：调用req.startAsync(request,response)方法，获取异步处理上下文对象AsyncContext
            final AsyncContext asyncContext = request.startAsync(request, response);

            //@1:设置异步处理超时时间
            Long timeout = Long.valueOf(request.getParameter("timeout"));
            asyncContext.setTimeout(timeout);
            //@3：用来异步处理是否结束，在这3个地方（子线程中处理完毕时、onComplete、onTimeout）将其更新为true
            final AtomicBoolean finish = new AtomicBoolean(false);
            //添加监听器
            asyncContext.addListener(new AsyncListener() {
                @Override
                public void onComplete(AsyncEvent event) throws IOException {
                    //异步处理完成会被回调
                    System.out.println(Thread.currentThread() + "-" + System.currentTimeMillis() + "-onComplete()");
                    if (finish.compareAndSet(false, true)) {
                        event.getAsyncContext().getRequest().setAttribute("result", "onComplete");
                        //转发请求
                        asyncContext.dispatch();
                    }
                }

                @Override
                public void onTimeout(AsyncEvent event) throws IOException {
                    //超时会被回调
                    System.out.println(Thread.currentThread() + "-" + System.currentTimeMillis() + "-onTimeout()");
                    if (finish.compareAndSet(false, true)) {
                        event.getAsyncContext().getRequest().setAttribute("result", "onTimeout");
                        //转发请求
                        asyncContext.dispatch();
                    }
                }

                @Override
                public void onError(AsyncEvent event) throws IOException {
                    //发生错误会被回调
                    System.out.println(Thread.currentThread() + "-" + System.currentTimeMillis() + "-onError()");
                    event.getAsyncContext().getResponse().getWriter().write("<br/>onError");
                }

                @Override
                public void onStartAsync(AsyncEvent event) throws IOException {
                    //开启异步请求调用的方法
                    System.out.println(Thread.currentThread() + "-" + System.currentTimeMillis() + "-onStartAsync()");
                    event.getAsyncContext().getResponse().getWriter().write("<br/>onStartAsync");
                }
            });
            //3、调用start方法异步处理，调用这个方法之后主线程就结束了
            asyncContext.start(new Runnable() {
                @Override
                public void run() {
                    long stSon = System.currentTimeMillis();
                    System.out.println("子线程：" + Thread.currentThread() + "-" + System.currentTimeMillis() + "-start");
                    try {
                        //@2:这里休眠2秒，模拟业务耗时
                        TimeUnit.SECONDS.sleep(2);
                        if (finish.compareAndSet(false, true)) {
                            asyncContext.getRequest().setAttribute("result", "ok");
                            //转发请求
                            asyncContext.dispatch();
                        }
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
