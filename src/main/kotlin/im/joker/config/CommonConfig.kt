package im.joker.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import im.joker.event.EventType
import im.joker.event.MessageType
import im.joker.helper.BCryptPasswordEncoder
import im.joker.helper.LocalDateTimeDeserializer
import im.joker.helper.LocalDateTimeSerializer
import im.joker.helper.PasswordEncoder
import org.apache.commons.lang3.StringUtils
import org.redisson.Redisson
import org.redisson.api.RedissonReactiveClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Component
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.cors.reactive.CorsUtils
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import java.time.LocalDateTime

/**
 * @Author: mkCen
 * @Date: 2020/11/21
 * @Time: 15:55
 * @Desc:
 */
@Component
class CommonConfig {


    @Value("\${spring.redis.database:0}")
    val redisDatabase: Int? = null

    @Value("\${spring.redis.host:localhost}")
    val redisHost: String? = null

    @Value("\${spring.redis.port:6379}")
    val redisPort: Int? = null

    @Value("\${spring.redis.password:}")
    val redisPassword: String? = null

    @Value("\${spring.redis.timeout:3000}")
    val redisTimeout: Int? = null


    @Bean
    fun redissonReactiveClient(): RedissonReactiveClient {
        val config = Config()
        val subConfig = config.useSingleServer()
        subConfig.address = "redis://$redisHost:$redisPort"
        subConfig.database = redisDatabase!!
        subConfig.timeout = redisTimeout!!
        if (StringUtils.isNotBlank(redisPassword)) {
            subConfig.password = redisPassword
        }
        return Redisson.createReactive(config)
    }

    /**
     * 多态子类事件注入
     *
     * @return
     */
    @Bean
    fun objectMapperBuilder(): Jackson2ObjectMapperBuilder {
        return object : Jackson2ObjectMapperBuilder() {
            override fun configure(objectMapper: ObjectMapper) {
                objectMapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
                for (e in EventType.values()) {
                    objectMapper.registerSubtypes(NamedType(e.eventClass, e.id))
                }
                for (e in MessageType.values()) {
                    objectMapper.registerSubtypes(NamedType(e.contentClass, e.id))
                }
                val module = JavaTimeModule()
                module.addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(LocalDateTime::class.java))
                module.addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(LocalDateTime::class.java))
                objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
                objectMapper.registerModule(module)
                super.configure(objectMapper)
            }
        }
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }


    /**
     * 往mongo里面存map的时候,用#代替.
     *
     * @param mongoConverter
     */
    @Autowired
    fun setMapKeyDotReplacement(mongoConverter: MappingMongoConverter) {
        mongoConverter.setMapKeyDotReplacement("#")
    }

    /**
     * WebFluxConfigurer 不知道为什么导致json的配置文件失效,因此这里用拦截器实现跨域
     *
     * @return
     */
    @Bean
    fun corsFilter(): WebFilter {
        return WebFilter { ctx: ServerWebExchange, chain: WebFilterChain ->
            val request = ctx.request
            if (CorsUtils.isCorsRequest(request)) {
                val response = ctx.response
                val headers = response.headers
                headers.add("Access-Control-Allow-Origin", "*")
                headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                headers.add("Access-Control-Max-Age", "18000L")
                headers.add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization")
                headers.add("Access-Control-Expose-Headers", "*")
                headers.add("Access-Control-Allow-Credentials", "true")
                if (request.method == HttpMethod.OPTIONS) {
                    response.statusCode = HttpStatus.OK
                }
            }
            chain.filter(ctx)
        }
    }

    @Bean
    fun validator(): LocalValidatorFactoryBean {
        return LocalValidatorFactoryBean()
    }

}