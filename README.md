# EDA Study

An Event-Driven Architecture (EDA) study project demonstrating asynchronous communication between microservices using Kafka, AWS S3, and Spring Boot with Kotlin.

## 📋 Project Overview

This project consists of two microservices that communicate asynchronously through Kafka:

- **Producer Service**: Generates and publishes order events to Kafka
- **Consumer Service** (Customer Service): Consumes order events from Kafka and stores data in S3

### Architecture Components

```
┌─────────────────────────────────────────────────────────────┐
│                    Producer Service                          │
│  - Spring Boot REST API                                      │
│  - Publishes OrderEvents to Kafka                            │
│  - Stores data in S3 (LocalStack in dev)                     │
└─────────────────────────────────────────────────────────────┘
                           │
                    (Kafka Topic)
                           │
┌─────────────────────────────────────────────────────────────┐
│                  Consumer Service                            │
│  - Spring Boot Application                                   │
│  - Listens to Kafka OrderEvents                              │
│  - Stores events in S3 (LocalStack in dev)                   │
└─────────────────────────────────────────────────────────────┘

Infrastructure:
- Kafka: Message broker for asynchronous communication
- Zookeeper: Kafka dependency for cluster coordination
- LocalStack: Local AWS S3 emulation for development
- Kafka UI: Web interface to monitor Kafka topics and messages
```

## 🛠️ Tech Stack

- **Language**: Kotlin 2.2.21
- **Framework**: Spring Boot 4.0.4
- **Message Broker**: Apache Kafka 7.5.0
- **AWS SDK**: v2 (with async/await support)
- **Build Tool**: Gradle
- **Java Version**: 21
- **Async Support**: Kotlin Coroutines

## 📦 Prerequisites

Before starting, ensure you have the following installed:

- **Java 21+**: [Download](https://adoptium.net/)
- **Docker & Docker Compose**: [Download](https://www.docker.com/products/docker-desktop)
- **Git**: [Download](https://git-scm.com/)

Verify installations:
```bash
java -version
docker --version
docker-compose --version
```

## 🚀 Quick Start Guide

### Step 1: Start Infrastructure Services

Navigate to the project root and start Kafka, Zookeeper, LocalStack, and Kafka UI using Docker Compose:

```bash
cd C:\Projetos\eda-study
docker-compose up -d
```

This will start:
- **Zookeeper** on port 2181
- **Kafka** on port 9092
- **LocalStack** (S3 emulation) on port 4566
- **Kafka UI** on port 8080

Verify services are running:
```bash
docker-compose ps
```

### Step 2: Start Producer Service

Open a new terminal and navigate to the producer service:

```bash
cd C:\Projetos\eda-study\producer-service
```

Build and run the application:

```bash
# For Linux/Mac
./gradlew bootRun

# For Windows
gradlew.bat bootRun
```

The Producer Service will start on **http://localhost:8080** (or next available port).

### Step 3: Start Consumer Service

Open another new terminal and navigate to the consumer service:

```bash
cd C:\Projetos\eda-study\costumer-service
```

Build and run the application:

```bash
# For Linux/Mac
./gradlew bootRun

# For Windows
gradlew.bat bootRun
```

The Consumer Service will start on **http://localhost:8081** (or next available port).

### Step 4: Verify Everything is Running

Check Docker containers:
```bash
docker-compose ps
```

Access Kafka UI to monitor messages:
```
http://localhost:8080
```

## 📝 Usage Examples

### Send an Order Event (Producer Service)

Once the Producer Service is running, send a POST request to create an order:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-001",
    "customerId": "CUST-001",
    "amount": 99.99,
    "timestamp": "2026-03-27T10:00:00Z"
  }'
```

Expected response:
```json
{
  "orderId": "ORD-001",
  "customerId": "CUST-001",
  "amount": 99.99,
  "timestamp": "2026-03-27T10:00:00Z",
  "status": "PUBLISHED"
}
```

### Monitor Kafka Messages

Open Kafka UI in your browser:
```
http://localhost:8080
```

Navigate to Topics → **orders-topic** to see published messages in real-time.

### Check S3 Storage (LocalStack)

View stored files in LocalStack S3:
```bash
aws s3 ls s3://eda-study-bucket --endpoint-url http://localhost:4566 --region us-east-1
```

To list objects:
```bash
aws s3api list-objects-v2 --bucket eda-study-bucket --endpoint-url http://localhost:4566 --region us-east-1
```

> **Note**: Install AWS CLI if not present: `pip install awscli`

## 📁 Project Structure

```
eda-study/
├── docker-compose.yml              # Infrastructure definition
├── README.md                        # This file
│
├── producer-service/               # Order event producer
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/
│   │   │   │   └── com/xssrae/producer_service/
│   │   │   │       ├── ProducerServiceApplication.kt
│   │   │   │       ├── controller/
│   │   │   │       │   └── OrderController.kt
│   │   │   │       ├── domain/
│   │   │   │       │   └── OrderEvent.kt
│   │   │   │       ├── producer/
│   │   │   │       │   └── OrderEventProducer.kt
│   │   │   │       └── service/
│   │   │   │           └── S3StorageService.kt
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   │       └── kotlin/
│   │           └── com/xssrae/producer_service/
│   │               └── ProducerServiceApplicationTests.kt
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradle/
│
├── costumer-service/               # Order event consumer
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/
│   │   │   │   └── com/xssrae/costumer_service/
│   │   │   │       ├── CostumerServiceApplication.kt
│   │   │   │       ├── config/
│   │   │   │       │   └── AppConfig.kt
│   │   │   │       ├── costumer/
│   │   │   │       │   ├── OrderEventConsumer.kt
│   │   │   │       │   └── costumer/
│   │   │   │       │       └── OrderEventCostumer.kt
│   │   │   │       ├── domain/
│   │   │   │       │   └── OrderEvent.kt
│   │   │   │       └── service/
│   │   │   │           └── S3StorageService.kt
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   │       └── kotlin/
│   │           └── com/xssrae/costumer_service/
│   │               └── CostumerServiceApplicationTests.kt
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradle/
│
└── localstack-data/                # LocalStack persistence volume
```

## 🔧 Configuration

### Environment Variables

Configure services using environment variables or `application.yml`:

**Kafka Configuration:**
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

**AWS S3 Configuration:**
```yaml
aws:
  region: us-east-1
  s3:
    bucket: eda-study-bucket
  endpoint: http://localhost:4566  # LocalStack endpoint
```

### Kafka Topics

The project automatically creates the following topics:

- **orders-topic**: Where order events are published and consumed

## 🛑 Stopping Services

### Stop Docker Containers

```bash
docker-compose down
```

To remove data volumes as well:
```bash
docker-compose down -v
```

### Stop Running Applications

- Press `Ctrl+C` in the terminals running Producer and Consumer services

## 🔍 Troubleshooting

### Issue: "Connection refused" to Kafka

**Solution**: Ensure Docker containers are running:
```bash
docker-compose up -d
docker-compose ps
```

### Issue: Producer service fails to start

**Solution**: Check if port 8080 is already in use. The application will use the next available port (8081, 8082, etc.).

To check which port is being used:
```bash
netstat -ano | findstr :8080
```

### Issue: Messages not appearing in Consumer

**Solution**: 
1. Verify the Consumer Service is running
2. Check that both services are connected to the same Kafka broker
3. Review logs for errors: check the terminal output of the Consumer Service

### Issue: LocalStack not persisting data

**Solution**: Ensure the `./localstack-data` volume is properly mounted and has write permissions:
```bash
docker-compose logs localstack
```

## 📚 Learning Resources

- [Event-Driven Architecture](https://en.wikipedia.org/wiki/Event-driven_architecture)
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Boot & Kafka](https://spring.io/projects/spring-kafka)
- [AWS SDK for Java v2](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

## 📄 License

This is a study project.

## 👤 Author

Created as an educational project for Event-Driven Architecture patterns.

---

**Last Updated**: March 2026

For issues or questions, please check the logs and Docker container status first!
