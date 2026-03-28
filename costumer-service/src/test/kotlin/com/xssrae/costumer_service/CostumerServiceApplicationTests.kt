package com.xssrae.costumer_service

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@Disabled("Requer infraestrutura Kafka + LocalStack rodando")
class CostumerServiceApplicationTests {

    @Test
    fun contextLoads() { }
}