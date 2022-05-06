import javax.servlet.AsyncContext;
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
@WebServlet(name = "s1",
        urlPatterns = "/as1",
        asyncSupported = true
)
public class AysnServer extends HttpServlet {
    @Override
    protected void service(HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        long st = System.currentTimeMillis();
        System.out.println("主线程：" + Thread.currentThread() + "-" + System.currentTimeMillis() + "-start");
        //2、启动异步处理：调用req.startAsync(request,response)方法，获取异步处理上下文对象AsyncContext
        final AsyncContext asyncContext = request.startAsync(request, response);
        //3、调用start方法异步处理，调用这个方法之后主线程就结束了
        asyncContext.start(new Runnable() {
            @Override
            public void run() {
                long stSon = System.currentTimeMillis();
                System.out.println("子线程：" + Thread.currentThread().toString() + "-" + System.currentTimeMillis() + "-start");
                try {
                    //这里休眠2秒，模拟业务耗时
//                    TimeUnit.SECONDS.sleep(2);
                    //这里是子线程，请求在这里被处理了
                    asyncContext.getResponse().getWriter().write("ok");
                    System.out.println("--------------:" + (response == asyncContext.getResponse()));
                    //4、调用complete()方法，表示请求请求处理完成
                    asyncContext.complete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("子线程：" + Thread.currentThread() + "-" + System.currentTimeMillis() + "-end,耗时(ms):" + (System.currentTimeMillis() - stSon));
            }
        });
        System.out.println("主线程：" + Thread.currentThread() + "-" + System.currentTimeMillis() + "-end,耗时(ms):" + (System.currentTimeMillis() - st));

    }

}

