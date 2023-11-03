package ed.maevski.diabeticdiary.di.modules

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import aws.smithy.kotlin.runtime.net.Url
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class S3Module {
    @Provides
    @Singleton
    fun provideCredentialsProvider(
        _accessKeyId: String = "YCAJEfdKt4435OxPCU24hKWmu",
        _secretAccessKey: String ="YCOrlDSR4otRxNwIyUjjlCHWHerdWuvIo13ld20x"
    ): CredentialsProvider = StaticCredentialsProvider {
        accessKeyId = _accessKeyId
        secretAccessKey = _secretAccessKey
    }

    @Provides
    @Singleton
    fun provideS3ClientYandex(
        _region: String = "ru-central1",
        _credentialsProvider: CredentialsProvider,
        _endpointUrl: String = "https://storage.yandexcloud.net"
    ): S3Client = S3Client {
        region = _region
        credentialsProvider = _credentialsProvider
        endpointUrl = Url.parse(_endpointUrl)
    }
}