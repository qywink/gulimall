package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

public class ThreadTest {
    public static ExecutorService service = Executors.newFixedThreadPool(10);
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main start..........");
        FutureTask<String> futureTask = new FutureTask<>(new Callable01());
        new Thread(futureTask).start();
        // 阻塞等待
        System.out.println(futureTask.get());
        System.out.println("main end..........");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(  5,
                 200,
                 10,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(  100000),
        Executors.defaultThreadFactory(),
        new ThreadPoolExecutor.AbortPolicy());
    }

    public static class Callable01 implements Callable {
        @Override
        public String call() throws Exception {
            System.out.println("start call ()");
            return "call方法执行完毕";
        }
    }
}
