package wang.angi.sample.reactor;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class StartupMain {
    static Logger logger = LoggerFactory.getLogger(StartupMain.class);

    public static void main(String[] args) {
        Flux.just(1, 2, 3, 4, 5, 6).subscribe(System.out::println);

        Flux.just(1, 2, 3, 4, 5, 6).subscribe(i -> {
            System.out.println(i / (i - 4));
        }, System.err::println);

        Flux.just(1, 2, 3, 4, 5, 6).subscribe(System.out::println, System.err::println, () -> System.out.println("over"));

        Flux.just(1, 2, 3, 4, 5, 6).subscribe(System.out::println, System.err::println, () -> System.out.println("over"), subscription -> {
            logger.info("subscriptionConsumer");
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
        Flux.error(new Exception("some error")).subscribe(System.out::println, System.err::println);
        Mono.just(1);
        Mono.justOrEmpty(1);
        Mono.justOrEmpty(null);
        Mono.justOrEmpty(Optional.ofNullable(null));

        Flux.just(0, 2, 3, 4, 5, 6).map(i -> {
            logger.info("map-1: " + i);
            return i / (i - 3);
        }).map(i -> {
            logger.info("map-2: " + i);
            return i * 1;
        }).subscribe(i -> {
            try {
                TimeUnit.SECONDS.sleep(19);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            logger.info("subscribe: "+i + "");
        }, System.err::println);

        logger.info("main over");
    }
}
