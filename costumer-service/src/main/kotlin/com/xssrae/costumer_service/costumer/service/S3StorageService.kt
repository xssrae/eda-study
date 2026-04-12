package com.xssrae.costumer_service.costumer.service

import com.xssrae.costumer_service.domain.OrderEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.future.await
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Object
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.time.LocalDate

//@Service
class S3StorageService (
    @Value("\${aws.s3.bucket:eda-study-bucket}")
    private val bucket: String,
    @Value("\${aws.region:us-east-1}")
    private val region: String,
    @Value("\${aws.s3.endpoint:http://localhost:4566}")
    private val endpointOverride: String,
    private val objectMapper: ObjectMapper
    ) {
    private val s3Client : S3AsyncClient by lazy {
        S3AsyncClient.builder()
            .region(Region.of(region))
            .apply {
                if (endpointOverride.isNotBlank()) {
                    endpointOverride(URI.create(endpointOverride))
                    forcePathStyle(true) // Necessário para S3 compatível, como MinIO
                }
            }
            .build()
    }
    private val log = LoggerFactory.getLogger(this::class.java)

    suspend fun persistEvent(event: OrderEvent) {
        val key = buildS3Key(event)
        val jsonData = objectMapper.writeValueAsString(event)

        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("application/json")
                .build(),
            AsyncRequestBody.fromBytes(jsonData.toByteArray())
        ).await()

        log.info("Evento persistido no S3: s3://$bucket/$key")
    }

    private fun buildS3Key(event: OrderEvent): String {
        val date = LocalDate.now()
        return "orders/${date.year}/${date.monthValue}/${date.dayOfMonth}/${event.orderId}.json"
    }

    fun listEventLogs(prefix: String): Flow<S3Object> = flow {
        val response = s3Client.listObjectsV2(
            ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build()
        ).await()

        for (item in response.contents()) {
            emit(item)
        }
    }
}