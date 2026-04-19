# EDA Study

Projeto de estudo de Arquitetura Orientada a Eventos (EDA) demonstrando comunicação assíncrona entre microsserviços utilizando Kafka, AWS S3 e Spring Boot com Kotlin.

## 📋 Visão Geral

O projeto é composto por dois microsserviços que se comunicam de forma assíncrona através do Kafka:

- **Producer Service**: Expõe uma API REST e publica eventos de pedido no Kafka
- **Consumer Service** (Costumer Service): Consome eventos de pedido do Kafka e, opcionalmente, persiste os dados no S3

### Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                    Producer Service (:8081)                  │
│  - API REST Spring Boot                                      │
│  - POST /orders → publica OrderEvent no Kafka               │
└─────────────────────────────────────────────────────────────┘
                           │
                    (orders-topic)
                           │
┌─────────────────────────────────────────────────────────────┐
│                  Consumer Service (:8082)                    │
│  - Aplicação Spring Boot                                     │
│  - @KafkaListener no orders-topic                            │
│  - S3StorageService (@Profile("aws") — desativado por padrão)│
└─────────────────────────────────────────────────────────────┘

Infraestrutura (Docker Compose):
- Kafka broker          → porta 9092 (externa) / 29092 (interna)
- Zookeeper             → porta 2181
- Kafka UI              → porta 8080  →  http://localhost:8080
- LocalStack (S3)       → porta 4566
```

## 🛠️ Tecnologias

- **Linguagem**: Kotlin
- **Framework**: Spring Boot 3.2.3
- **Message Broker**: Apache Kafka (confluentinc/cp-kafka:7.5.0)
- **AWS SDK**: v2 (software.amazon.awssdk, async/await via kotlinx-coroutines-jdk8)
- **Build**: Gradle (build.gradle.kts)
- **Java**: 21
- **Suporte Assíncrono**: Kotlin Coroutines

## 📦 Pré-requisitos

- **Java 21+**: [Download](https://adoptium.net/)
- **Docker & Docker Compose**: [Download](https://www.docker.com/products/docker-desktop)
- **Git**: [Download](https://git-scm.com/)

Verifique as instalações:
```bash
java -version
docker --version
docker-compose --version
```

## 🚀 Iniciando o Projeto

### Passo 1: Subir a Infraestrutura

```bash
cd eda-study
docker-compose up -d
docker-compose ps   # todos os serviços devem exibir "Up"
```

| Container   | Porta | Finalidade                              |
|-------------|-------|-----------------------------------------|
| zookeeper   | 2181  | Coordenação do Kafka                    |
| kafka       | 9092  | Broker (externa / Spring Boot)          |
| kafka       | 29092 | Broker (interna / Docker)               |
| kafka-ui    | 8080  | Interface web → http://localhost:8080   |
| localstack  | 4566  | Emulação do S3                          |

> ⚠️ Aguarde ~20 segundos após o `docker-compose up` antes de iniciar os serviços — o Kafka precisa de alguns instantes para inicializar.

### Passo 2: Iniciar o Producer Service

```bash
cd eda-study/producer-service

# Linux/Mac
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

O Producer inicia em **http://localhost:8081**

### Passo 3: Iniciar o Consumer Service

Abra um novo terminal:

```bash
cd eda-study/costumer-service

# Linux/Mac
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

O Consumer inicia em **http://localhost:8082**

## 📝 Enviando um Evento de Pedido

### Via curl

```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "order-001",
    "costumerId": "cliente-123",
    "items": [
      { "productId": "prod-A", "quantity": 2, "price": 49.90 },
      { "productId": "prod-B", "quantity": 1, "price": 99.90 }
    ],
    "totalAmount": 199.70,
    "status": "PENDING"
  }'
```

### Via PowerShell

```powershell
Invoke-RestMethod -Uri "http://localhost:8081/orders" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{
    "orderId": "order-001",
    "costumerId": "cliente-123",
    "items": [
      { "productId": "prod-A", "quantity": 2, "price": 49.90 },
      { "productId": "prod-B", "quantity": 1, "price": 99.90 }
    ],
    "totalAmount": 199.70,
    "status": "PENDING"
  }'
```

Resposta esperada:
```json
{
  "message": "Evento de pedido enviado com sucesso"
}
```

> ⚠️ **Atenção**: o campo é `costumerId` (com **o**) — deve bater exatamente com o DTO.

### Log esperado no Consumer

Após enviar a requisição, o terminal do consumer deve exibir:

```
✅ Evento recebido!
   orderId   = order-001
   costumerId= cliente-123
   status    = PENDING
   total     = 199.70
   partition = 0 | offset = 0
📦 Processando pedido order-001
```

### Via Kafka UI

1. Acesse **http://localhost:8080**
2. Vá em **Topics → orders-topic → Produce Message**
3. Cole o payload JSON e clique em **Produce**

## 📁 Estrutura do Projeto

```
eda-study/
├── docker-compose.yml
├── README.md
│
├── producer-service/
│   ├── src/main/kotlin/com/xssrae/producer_service/
│   │   ├── ProducerServiceApplication.kt
│   │   ├── config/
│   │   │   ├── KafkaConfig.kt          # beans KafkaTemplate + ProducerFactory
│   │   │   └── KafkaTopicConfig.kt     # cria o orders-topic automaticamente
│   │   ├── controller/
│   │   │   └── OrderController.kt      # POST /orders
│   │   ├── domain/
│   │   │   ├── OrderEvent.kt           # data class
│   │   │   ├── OrderItem.kt            # data class
│   │   │   └── OrderStatus.kt          # sealed class
│   │   └── producer/
│   │       └── OrderEventProducer.kt   # suspend fun + coroutines
│   ├── src/main/resources/application.yml
│   ├── src/test/resources/application.yml
│   └── build.gradle.kts
│
├── costumer-service/
│   ├── src/main/kotlin/com/xssrae/costumer_service/
│   │   ├── CostumerServiceApplication.kt
│   │   ├── config/
│   │   │   └── AppConfig.kt            # bean ObjectMapper
│   │   ├── costumer/
│   │   │   └── OrderEventCostumer.kt   # @KafkaListener
│   │   ├── domain/
│   │   │   ├── OrderEvent.kt           # data class (status: String)
│   │   │   └── OrderItem.kt
│   │   └── service/
│   │       └── S3StorageService.kt     # @Profile("aws") — desativado localmente
│   ├── src/main/resources/application.yml
│   ├── src/test/resources/application.yml
│   └── build.gradle.kts
│
└── localstack-data/                    # volume de persistência do LocalStack
```

## 🔧 Configuração

### Mapa de Portas

| Serviço          | Porta |
|------------------|-------|
| Kafka UI         | 8080  |
| Producer Service | 8081  |
| Consumer Service | 8082  |
| Kafka (externa)  | 9092  |
| LocalStack S3    | 4566  |
| Zookeeper        | 2181  |

### Principais propriedades do `application.yml`

```yaml
# Ambos os serviços
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

app:
  kafka:
    topics:
      orders: orders-topic

aws:
  region: ${AWS_REGION:us-east-1}
  s3:
    bucket: ${S3_BUCKET:eda-study-bucket}
    endpoint: ${LOCALSTACK_ENDPOINT:http://localhost:4566}
```

### Ativando a integração com S3

O `S3StorageService` está anotado com `@Profile("aws")` e **não é carregado por padrão**.

Para ativá-lo, inicie o consumer com o profile `aws`:

```bash
./gradlew bootRun --args='--spring.profiles.active=aws'
```

Certifique-se de que o LocalStack está rodando e o bucket foi criado antes de ativar.

## 🛑 Encerrando os Serviços

```bash
# Para todos os containers Docker
docker-compose down

# Para e remove os volumes (limpa dados do Kafka e LocalStack)
docker-compose down -v
```

Encerre os serviços Spring Boot com `Ctrl+C` em cada terminal.

## 🔍 Solução de Problemas

### Consumer não recebe mensagens

1. Confirme que ambos os serviços usam o mesmo nome de tópico (`orders-topic`)
2. Verifique se `app.kafka.topics.orders` existe no `application.yml` do consumer
3. Procure nos logs do consumer por `Subscribed to topic(s): orders-topic`
4. Confirme que o `@KafkaListener` referencia `${app.kafka.topics.orders}`

### Kafka UI não conecta ao broker

O Kafka UI roda **dentro do Docker** e precisa usar o listener interno. Certifique-se de que o `docker-compose.yml` possui:

```yaml
kafka:
  environment:
    KAFKA_LISTENERS: INTERNAL://0.0.0.0:29092,EXTERNAL://0.0.0.0:9092
    KAFKA_ADVERTISED_LISTENERS: INTERNAL://kafka:29092,EXTERNAL://localhost:9092
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: INTERNAL:PLAINTEXT,EXTERNAL:PLAINTEXT
    KAFKA_INTER_BROKER_LISTENER_NAME: INTERNAL

kafka-ui:
  environment:
    KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka:29092   # listener interno
```

### Conflito de portas

```powershell
# Encontrar processo usando uma porta (Windows)
netstat -ano | findstr :8081

# Encerrar pelo PID
taskkill /PID <PID> /F
```

### `PlaceholderResolutionException` na inicialização

Adicione valores padrão em todas as anotações `@Value`:

```kotlin
@Value("\${aws.s3.bucket:eda-study-bucket}") private val bucket: String
@Value("\${aws.region:us-east-1}") private val region: String
@Value("\${aws.s3.endpoint:http://localhost:4566}") private val endpoint: String
```

### Bean `KafkaTemplate` não encontrado

Certifique-se de que o arquivo `KafkaConfig.kt` existe dentro de `config/` e declara os beans `producerFactory` e `kafkaTemplate`. Consulte `producer-service/config/KafkaConfig.kt`.

### 400 Bad Request no POST /orders

Verifique se o corpo da requisição bate exatamente com os campos do DTO — especialmente `costumerId` (com **o**, não `customerId`).

## 🚀 Futuras Melhorias

- **Armazenamento via Bucket S3**: ativar o `S3StorageService` no consumer para persistir cada evento consumido como um arquivo JSON no S3, utilizando o LocalStack em desenvolvimento e um bucket real na AWS em produção. A integração já está implementada e desativada via `@Profile("aws")`, aguardando validação do fluxo completo do Kafka antes de ser habilitada.

## 📚 Recursos de Aprendizado

- [Arquitetura Orientada a Eventos](https://en.wikipedia.org/wiki/Event-driven_architecture)
- [Documentação do Apache Kafka](https://kafka.apache.org/documentation/)
- [Spring Kafka Reference](https://docs.spring.io/spring-kafka/reference/)
- [AWS SDK para Java v2](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/)
- [Guia de Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Documentação do LocalStack](https://docs.localstack.cloud/)

## 📄 Licença

Projeto de estudo — livre para uso e modificação.

---

**Última atualização**: Abril de 2026