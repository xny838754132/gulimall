package com.nai.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * @author TheNai
 * @date 2021-03-07 14:30
 */
public class ThreadTest {
    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws Exception {
        System.out.println("main...start...");
        /*
         * 方法完成后的感知
         */
//        CompletableFuture.runAsync(()->{
//            System.out.println("当前线程:" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果:" + i);
//        },executor);
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程:" + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("运行结果:" + i);
//            return i;
//        }, executor).whenComplete((result,exception)->{
//            //虽然能得到异常信息，但是无法修改返回数据
//            System.out.println("异步任务成功完成了。。。结果是："+result+",异常是："+exception);
//        }).exceptionally(throwable -> {
//            //可以感知异常，同时返回默认值
//            return 10;
//        });
        //R apply(T t);
//

        /*
         * 方法执行完成后的处理
         */
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程:" + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("运行结果:" + i)；
//            return i;
//        }, executor).handle((result,thr)->{
//            if (result!=null){
//                return result*2;
//            }
//            if (thr!=null) {
//                return 0;
//            }
//            return 0;
//        });
        /*
            线程串行化
            1. thenRun：不能获取到上一步的执行结果，无返回值
                .thenRunAsync(() -> {
            System.out.println("任务2启动了。。。")；
            }, executor);
            2.thenAcceptAsync能接收上一步结果，但是无返回值
            3.thenApplyAsync能接收上一步的结果，有返回值

         */
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程:" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果:" + i);
//            return i;
//        }, executor).thenApplyAsync(res -> {
//            System.out.println("任务2启动了。。。" + res);
//            return "Hello" + res;
//        }, executor);
//        String s = future.get();
        //future.get() 阻塞方法

        /*
            两个都完成
         */
//        CompletableFuture<Object> future01 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1线程:" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("任务1结束:" + i);
//            return i;
//        }, executor);
//
//        CompletableFuture<Object> future02 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务2线程:" + Thread.currentThread().getId());
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("任务2结束");
//            return "Hello";
//        }, executor);
//        future01.runAfterBothAsync(future02,()->{
//            System.out.println("任务3开始");
//        },executor);
//        future01.thenAcceptBothAsync(future02,(f1,f2)->{
//            System.out.println("任务3开始...之前的结果："+f1+"---"+f2);
//        },executor);
//        CompletableFuture<String> future = future01.thenCombineAsync(future02, (f1, f2) -> {
//            return f1 + ":" + f2 + "->HAhA";
//        }, executor);
//        System.out.println("main...end..."+future.get() );

        //两个任务只要有一个完成，我们就执行任务3
        //runAfterEitherAsync 不感知结果，自己也无返回值
//        future01.runAfterEitherAsync(future02,()->{
//            System.out.println("任务3开始");
//        },executor);
        //acceptEitherAsync 感知结果，自己没有返回值
//        future01.acceptEitherAsync(future02,(res)->{
//            System.out.println("任务3开始"+res);
//        },executor);
//        CompletableFuture<String> future = future01.applyToEitherAsync(future02, res -> {
//            System.out.println("任务3开始");
//            return res.toString() + "哈哈";
//        }, executor);
        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的图片信息");
            return "hello.jpg";
        }, executor);
        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的属性");
            return "hello.jpg";
        }, executor);
        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("查询商品的介绍");
            return "hello.jpg";
        }, executor);
//        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futureAttr, futureDesc);
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureImg, futureAttr, futureDesc);
        anyOf.get();
        //等待所有结果完成
        System.out.println("main...end..." + futureImg.get() + "=>" + futureAttr.get() + "=>" + futureDesc.get());

    }


    public void thread(String[] args) throws Exception {
        System.out.println("main...start...");
        /**
         * 1).继承Thread
         *      Thread01 thread01 = new Thread01();
         *         thread01.start()；
         * 2).实现Runnable接口
         * Runnable01 runnable01 = new Runnable01()；
         *         new Thread(runnable01).start()；
         * 3).实现Callable接口+FutureTask(可以拿到返回结果,可以处理异常)
         * FutureTask<Integer> futureTask = new FutureTask<>(new Callable01())；
         *         new Thread(futureTask).start()；
         *         等待整个线程执行完成获取返回结果
         *         阻塞等待整个线程执行完成,获取返货结果
         *         Integer integer = futureTask.get()；
         * 4).线程池[ExecutorService]
         *      给线程池直接提交任务
         *      1、创建：
         *          1）、Executors
         *
         * 区别：
         *  1，2 不能得到返回值。第三种方式可以获取返回值
         *  1，2，3 都不能控制资源
         *  4 可以控制资源，整个系统的性能是稳定的。
         *
         *  在业务代码中，以上三种启动线程的方式，都不要用。应该将所有的多线程异步任务，都交给线程池执行。
         *
         *  当前系统中池只有1 2个，每个异步任务，提交给线程池让他自己去执行就行
         *
         * 七大参数：
         * int corePoolSize：[5]  核心线程数【一直存在，除非（allowCoreThreadTimeOut）】；线程池创建好以后就准备就绪的线程数量，就等待来接收异步任务去执行
         *          5个 Thread thread = new Thread(); thread.start()
         *
         * int maximumPoolSize：[200] 最大线程数量； 控制资源
         *
         * long keepAliveTime： 存活时间。如果当前线程数量大于核心线程数量[corePoolSize]。
         *          释放空闲的线程（maximumPoolSize-corePoolSize）。只要线程空闲大于指定的keepAliveTime
         *
         * TimeUnit unit：时间单位
         * BlockingQueue<Runnable> workQueue：阻塞队列。如果任务有很多，就会将目前多的任务放在队列里面。
         *          只要有线程空闲就会从队列里面取出新的任务继续执行。
         *
         * ThreadFactory threadFactory：线程的创建工厂
         *
         * RejectedExecutionHandler handler：如果队列满了，按照我们指定的拒绝测率，拒绝执行任务
         *
         * 工作顺序：
         * 1).线程池创建了，准备好core数量的核心线程，准备接收任务
         * 1.1).core满了，就将再进来的任务放入阻塞队列中。空闲的core就会自己去阻塞队列获取任务执行
         * 1.2).阻塞队列满了，就直接开新线程执行，最大只能开到max指定的数量
         * 1.3).max满了就用RejectedExecutionHandler绝任务
         * 1.4).max都执行完成，有很多空闲，在指定的时间keepAliveTime以后，释放max-core这些线程
         *    new LinkedBlockingQueue<>()：默认是Integer的最大值。可能导致内存不够。
         *
         * 面试题：
         *   一个线程池 core：7  max：20  queue：50 100个并发进来怎么分配
         *   7个会立即得到执行，50个会进入队列，再开13个进行执行。剩下的30个就使用拒绝测率来执行
         *   如果不想抛弃，还要执行，使用CallerRunsPolicy；
         */
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
        //带缓存的线程池核心是0，所有都可以回收
        Executors.newCachedThreadPool();
        //固定大小线程池 core=max；都不可以回收
        Executors.newFixedThreadPool(100);
        //定时任务的线程池
        Executors.newScheduledThreadPool(100);
        //单线程的线程池 后台从队列里面获取任务，挨个执行
        Executors.newSingleThreadExecutor();
        System.out.println("main...end...");
    }

    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程:" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果:" + i);
        }
    }

    public static class Runnable01 implements Runnable {
        @Override
        public void run() {
            System.out.println("当前线程:" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果:" + i);
        }
    }

    public static class Callable01 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程:" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果:" + i);
            return i;
        }
    }

}
