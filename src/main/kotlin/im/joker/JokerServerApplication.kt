package im.joker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class JokerServerApplication

fun main(args: Array<String>) {
    runApplication<JokerServerApplication>(*args)
}
