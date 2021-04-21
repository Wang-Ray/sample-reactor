package wang.angi.sample.reactor;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ReactorTest {
    static Logger logger = LoggerFactory.getLogger(ReactorTest.class);

    @Test
    public void testNormal() throws Exception {
        StepVerifier.create(Flux.range(1, 6))
                .expectNext(1, 2, 3, 4, 5, 6)
                .expectComplete()
                .verify();
    }

    @Test
    public void testComplete() {
        StepVerifier.create(Mono.empty())
                .expectComplete()
                .verify();
    }

    @Test
    public void testError() {
        StepVerifier.create(Mono.error(new Exception("some error")))
                .expectErrorMessage("some error")
                .verify();
    }

    @Test
    public void testFromRunnable() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Mono.fromRunnable(() -> {
            try {
                logger.info("begin Runnable");
                TimeUnit.SECONDS.sleep(2);
                logger.info("end Runnable");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        })
                .subscribe(null, null, countDownLatch::countDown);
        logger.info("main over");
        countDownLatch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testFromCallable() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Mono.fromCallable(() -> {
            try {
                logger.info("begin Callable");
                TimeUnit.SECONDS.sleep(2);
                logger.info("end Callable");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Hello, Reactor!";
        }).map(str -> {
            logger.info("map: " + str);
            return str + ", Welcome!";
        })
                .subscribe(logger::info, null, countDownLatch::countDown);
        logger.info("main over");
        countDownLatch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testFromSupplier() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Mono.fromSupplier(() -> {
            try {
                logger.info("begin Supplier");
                TimeUnit.SECONDS.sleep(2);
                logger.info("end Supplier");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "Hello, Reactor!";
        }).map(str -> {
            logger.info("map: " + str);
            return str + ", Welcome!";
        })
                .subscribe(logger::info, null, countDownLatch::countDown);
        logger.info("main over");
        countDownLatch.await(10, TimeUnit.SECONDS);
    }

    private CompletableFuture<String> getCompletableFuture(String name) {
        logger.info("begin getCompletableFuture");
        final CompletableFuture<String> completableFuture = new CompletableFuture<>();
        // 模拟其他线程完成future（比如Dubbo异步调用应答）
        new Thread(() -> {
            logger.info("begin sleep");
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("end sleep");
            completableFuture.complete("hello, " + name);
        }).start();
        logger.info("end getCompletableFuture");
        return completableFuture;
    }

    @Test
    public void testFromFuture() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Mono.fromFuture(getCompletableFuture("ray"))
                // 接下来由future完成线程执行
                .map(str -> {
                    logger.info("map: " + str);
                    return str + ", welcome!";
                })
                .subscribe(logger::info, null, countDownLatch::countDown);
        logger.info("main over");
        countDownLatch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testGenerate1() {
        final AtomicInteger count = new AtomicInteger(1);   // 1
        Flux.generate(sink -> {
            logger.info("begin sink: " + count.get());
            // emit signal
            sink.next(count.get() + " : " + new Date());   // 2
//            try {
//                TimeUnit.SECONDS.sleep(1);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            if (count.getAndIncrement() >= 5) {
                // emit signal
                sink.complete();     // 3
            }
            logger.info("end sink: " + count.get());
        }).subscribe(t -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info(t.toString());
        }, System.err::println, () -> {
            logger.info("complete");
        });  // 4
        logger.info("main over!");
    }

    @Test
    public void testGenerate2() {
        Flux.generate(
                () -> 1,    // 1
                (count, sink) -> {      // 2
                    sink.next(count + " : " + new Date());
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (count >= 5) {
                        sink.complete();
                    }
                    return count + 1;   // 3
                }).subscribe(System.out::println);
    }

    @Test
    public void testGenerate3() {
        Flux.generate(
                () -> 1,
                (count, sink) -> {
                    sink.next(count + " : " + new Date());
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (count >= 5) {
                        sink.complete();
                    }
                    return count + 1;
                }, System.out::println)     // 1
                .subscribe(System.out::println);
    }

    @Test
    public void testMap() {
        StepVerifier.create(
                Flux.range(0, 6)
                        .map(i -> i * 2)
                        .doOnNext(System.out::println))
                .expectNext(0, 2, 4, 6, 8, 10).verifyComplete();
    }

    @Test
    public void testFlatMap() {
        // d/a/e/b/f/c
        StepVerifier.create(
                Flux.just("abc", "def")
                        .flatMap(i -> Flux.fromArray(i.split("\\s*"))
                                .delayElements(Duration.ofMillis(100)))
                        .doOnNext(System.out::println))
                .expectNextCount(6).verifyComplete();
    }

    @Test
    public void testConcatMap() {
        // a/b/c/d/e/f
        StepVerifier.create(
                Flux.just("abc", "def")
                        .concatMap(i -> Flux.fromArray(i.split("\\s*"))
                                .delayElements(Duration.ofMillis(100)))
                        .doOnNext(System.out::println))
                .expectNextCount(6).verifyComplete();
    }

    @Test
    public void testFlatMapSequential() {
        // a/b/c/d/e/f
        StepVerifier.create(
                Flux.just("abc", "def")
                        .flatMapSequential(i -> Flux.fromArray(i.split("\\s*"))
                                .delayElements(Duration.ofMillis(100)))
                        .doOnNext(System.out::println))
                .expectNextCount(6).verifyComplete();
    }

    @Test
    public void testFilter() {
        StepVerifier.create(
                Flux.range(1, 6)
                        .filter(i -> i % 2 == 1)
                        .map(i -> i * i))
                .expectNext(1, 9, 25)
                .verifyComplete();
    }

    @Test
    public void testZip() {
        StepVerifier.create(
                Flux.zip(Flux.range(0, 2), Flux.range(5, 2)).doOnNext(System.out::println))
                .expectNextCount(2)
                .verifyComplete();

    }
}
