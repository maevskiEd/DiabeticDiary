package ed.maevski.diabeticdiary.utils

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.ListObjectsV2Request
import aws.sdk.kotlin.services.s3.model.Object
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.net.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

class S3ClientYandex (private val _accessKeyId: String, private val _secretAccessKey: String) {

    private val s3Client: S3Client

    init {
        val _credentialsProvider = StaticCredentialsProvider {
            accessKeyId = _accessKeyId
            secretAccessKey = _secretAccessKey
        }

        s3Client = S3Client {
            region = REGION
            credentialsProvider = _credentialsProvider
            endpointUrl = Url.parse(ENDPOINT)

        }
    }

    suspend fun uploadFile(bucketName: String, nameFile: String) {
        val request = PutObjectRequest {
            bucket = bucketName
            key = nameFile
            body = ByteStream.fromString("Test")
        }

        try {
            // Выполняем загрузку файла в бакет
            s3Client.putObject(request)
            println("Файл успешно загружен в бакет: $bucketName")
        } catch (e: Exception) {
            // Обработка ошибок
            println("Произошла ошибка при загрузке файла в бакет: ${e.message}")
        }
    }

    suspend fun listObjectsInBucket(scope: CoroutineScope, bucketName: String): List<Object>? {

        var objects: List<Object>? = null
        val request = ListObjectsV2Request {
            bucket = bucketName
        }

        val deffered = scope.async {
            try {
                val response = s3Client.listObjectsV2(request)
                objects = response.contents
                objects
            } catch (e: Exception){
                // Обработка ошибок
                println("Произошла ошибка при получении списка объектов: ${e.message}")
                null
            }
        }.await()
        return deffered
    }

    fun close() {
        s3Client.close()
    }
    companion object {
        private val REGION = "ru-central1"
        private val ENDPOINT = "https://storage.yandexcloud.net"
    }

}