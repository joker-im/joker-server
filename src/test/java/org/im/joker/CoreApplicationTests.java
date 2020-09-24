package org.im.joker;

import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.time.Duration;

@SpringBootTest
class CoreApplicationTests {


    public static void main(String[] args) {
        Flux.just("a", "v", "c")
                .delayElements(Duration.ofSeconds(3))
                .subscribe(System.out::println)
        ;
    }

}
