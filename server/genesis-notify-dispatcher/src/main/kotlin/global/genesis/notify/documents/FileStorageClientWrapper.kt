package global.genesis.notify.documents

import global.genesis.commons.annotation.Module
import global.genesis.file.client.FileStorageClient
import global.genesis.file.message.common.FileContentReply
import global.genesis.file.message.event.EventGenerateDocumentContentReply
import global.genesis.file.message.event.GenerateDocumentContent
import global.genesis.file.message.request.FileContentRequest
import global.genesis.gen.dao.Notify
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.inject.Inject

data class GeneratedDocument(
    val content: String,
    val assets: List<FileContentReply>
)

private const val NOTIFY_DISPATCHER = "NOTIFY_DISPATCHER"

/**
 * Wrapper class for translating between notify rel
 */
@Module
class FileStorageClientWrapper @Inject constructor(
    private val fileStorageClient: FileStorageClient
) {
    suspend fun getFileContents(fileStorageId: String): FileContentReply? {
        val request = FileContentRequest(
            fileStorageIds = setOf(fileStorageId),
            fileNames = emptySet()
        )
        val replies = requestFileContents(request)
        return if (replies.isEmpty()) {
            throw IllegalArgumentException(
                "Received empty reply while attempting to request file contents from " +
                    "FILE_MANAGER with file storage id $fileStorageId, document ID may be invalid"
            )
        } else {
            if (replies.size > 1) {
                throw IllegalArgumentException("Received unexpected multiple results for request to FILE_MANAGER with single file storage id $fileStorageId")
            }
            replies.first()
        }
    }

    suspend fun getAllFileContents(fileStorageIds: Set<String>): List<FileContentReply> {
        val request = FileContentRequest(
            fileStorageIds = fileStorageIds,
            fileNames = emptySet()
        )
        return requestFileContents(request)
    }

    private suspend fun requestFileContents(request: FileContentRequest): List<FileContentReply> {
        return try {
            fileStorageClient.getFileContents(request, NOTIFY_DISPATCHER)
        } catch (e: Exception) {
            throw IllegalArgumentException("Exception caught while attempting to request file contents from FILE_MANAGER with request $request", e)
        }
    }

    suspend fun generateDocumentContent(notify: Notify): GeneratedDocument {
        if (notify.templateRef != null) {
            val templateReference = notify.templateRef!!
            val details = GenerateDocumentContent(
                templateReference,
                mapOf(
                    "header" to notify.header,
                    "body" to notify.body
                ),
                notify.tableName,
                notify.tableEntityId
            )

            val reply = fileStorageClient.generateDocumentContent(details, NOTIFY_DISPATCHER)

            return when (reply) {
                is EventGenerateDocumentContentReply.EventGenerateDocumentContentAck -> {
                    GeneratedDocument(String(reply.content, Charsets.UTF_8), reply.assets)
                }
                is EventGenerateDocumentContentReply.EventGenerateDocumentContentNack -> {
                    val message =
                        "Received NACK response from document content generation request $details, id $templateReference may be invalid"
                    throw IllegalArgumentException(message)
                }
                null -> {
                    val message =
                        "Received null response from document content generation request $details, FILE_MANAGER may be down"
                    throw IllegalArgumentException(message)
                }
            }
        }
        return GeneratedDocument(notify.body, emptyList())
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(FileStorageClient::class.java)
    }
}
