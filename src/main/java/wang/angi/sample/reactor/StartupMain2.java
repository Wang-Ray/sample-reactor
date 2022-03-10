package wang.angi.sample.reactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;

public class StartupMain2 {
	static Logger logger = LoggerFactory.getLogger(StartupMain2.class);

	public static void main(String[] args) {
		logger.info("main begin");
		Flux.just(1, 2, 3, 4, 5, 6).map(i -> {
			logger.info("map-1-*: " + i);
			return i * 2;
		}).map(i -> {
			logger.info("map-2-/: " + i);
			return i / 2;
		}).subscribe(i -> {
			logger.info("consumer: " + i);
		}, t -> {
			logger.error("errorConsumer: " + t);
		}, () -> {
			logger.info("completeConsumer");
		}, subscription -> {
			logger.info("subscriptionConsumer");
			subscription.request(Long.MAX_VALUE);
		});
		logger.info("main over");
	}
}
