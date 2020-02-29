package wang.angi.sample.reactor;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class StartupMain {
    static Logger logger = LoggerFactory.getLogger(StartupMain.class);

    public static void main(String[] args) {
        Flux.just(1, 2, 3, 4, 5, 6).subscribe(System.out::println);

        Flux.just(1, 2, 3, 4, 5, 6).subscribe(i -> {
            System.out.println(i / (i - 4));
        }, System.err::println);

        Flux.just(1, 2, 3, 4, 5, 6).subscribe(System.out::println, System.err::println, () -> System.out.println("over"));

        Flux.just(1, 2, 3, 4, 5, 6).subscribe(System.out::println, System.err::println, () -> System.out.println("over"), subscription -> {
            System.out.println("subscribe");
            subscription.request(1);
        });

        Flux.just(1, 2, 3, 4, 5, 6).subscribe(new BaseSubscriber<Integer>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                super.hookOnSubscribe(subscription);
                logger.info("hookOnSubscribe: {}", subscription);
            }

            @Override
            protected void hookOnNext(Integer value) {
                super.hookOnNext(value);
                logger.info("hookOnNext: {}", value);
            }

            @Override
            protected void hookOnComplete() {
                super.hookOnComplete();
                logger.info("hookOnComplete");
            }

            @Override
            protected void hookOnError(Throwable throwable) {
                super.hookOnError(throwable);
                logger.error("hookOnError: {}", throwable);
            }
        });

        Flux.empty();
        Flux.never();
        Flux.error(new Exception("some error"));
        Mono.just(1);
        Mono.justOrEmpty(1);
        Mono.justOrEmpty(null);
        Mono.justOrEmpty(Optional.ofNullable(null));

        Flux.just(1, 2, 3, 4, 5, 6).map(i -> {
            logger.info("map0: " + i);
            return i / (i - 4);
        }).map(i -> {
            logger.info("map1: " + i);
            return i * 2;
        }).subscribe(i -> {
            try {
                TimeUnit.SECONDS.sleep(20);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            logger.info(i + "");
        }, System.err::println);
        logger.info("main over");
    }
}
