package global.genesis.notify.gateway.email

import com.google.inject.Inject
import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.file.message.common.FileContentReply
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.NotifyAttachment
import global.genesis.notify.documents.FileStorageClientWrapper
import global.genesis.notify.documents.MimeTypeMap
import global.genesis.notify.gateway.sendgrid.forEachUniqueStorageId
import global.genesis.notify.message.common.EmailDistribution
import global.genesis.notify.message.common.splitIntoDisplayNameAndAddress
import global.genesis.session.UserEmailCache
import kotlinx.coroutines.flow.toList
import org.simplejavamail.api.email.Email
import org.simplejavamail.api.email.EmailPopulatingBuilder
import org.simplejavamail.email.EmailBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Utility class for building [Email] objects from [Notify] objects.
 *
 * Handles all steps in building the email, including:
 * - Decomposition of the [EmailDistribution] to individual email addresses
 * - lookups of email addresses for individual users
 * - resolution of documents from the document store for email attachments
 *
 * @author tgosling
 */
class EmailBuilder @Inject constructor(
    private val config: EmailGatewayConfig,
    private val emailCache: UserEmailCache,
    private val db: AsyncEntityDb,
    private val fileStorageClient: FileStorageClientWrapper

) {
    suspend fun buildEmail(recipientUserName: String?, message: Notify): Email {
        val email = emailCache.userEmailAddress(recipientUserName)
            ?: throw RuntimeException("Could not obtain email address for user $recipientUserName")
        val emailDistribution = EmailDistribution("$recipientUserName <$email>")
        return buildEmail(emailDistribution, message)
    }

    suspend fun buildEmail(emailDistribution: EmailDistribution, message: Notify): Email {
        return EmailBuilder.startingBlank()
            .from(message)
            .addDestinations(emailDistribution)
            .withSubject(message.header)
            .addBody(message)
            .addAttachments(message)
            .buildEmail()
    }

    private fun EmailPopulatingBuilder.from(message: Notify): EmailPopulatingBuilder {
        return if (message.sender != null && config.sendFromUserAddress) {
            from(emailCache.userEmailAddress(message.sender, config.systemDefaultEmail!!))
        } else {
            from(config.systemDefaultEmail!!)
        }
    }

    private fun EmailPopulatingBuilder.addDestinations(emailDistribution: EmailDistribution): EmailPopulatingBuilder {
        var builder = this

        for (email in emailDistribution.to) {
            val (displayName, emailAddress) = email.splitIntoDisplayNameAndAddress()
            builder = builder.to(displayName, emailAddress)
        }
        for (email in emailDistribution.cc) {
            val (displayName, emailAddress) = email.splitIntoDisplayNameAndAddress()
            builder = builder.cc(displayName, emailAddress)
        }
        for (email in emailDistribution.bcc) {
            val (displayName, emailAddress) = email.splitIntoDisplayNameAndAddress()
            builder = builder.bcc(displayName, emailAddress)
        }

        return builder
    }

    private suspend fun EmailPopulatingBuilder.addBody(notify: Notify): EmailPopulatingBuilder {
        val documentContentResult = fileStorageClient.generateDocumentContent(notify)
        if (documentContentResult.assets.isNotEmpty()) {
            documentContentResult.assets.forEach {
                val fileExtension = it.fileName.getFileExtension()
                this.withEmbeddedImage(
                    it.fileName,
                    it.fileContent,
                    MimeTypeMap.getMimeType(fileExtension)
                )
            }
        }
        return withHTMLText(documentContentResult.content)
    }

    private suspend fun EmailPopulatingBuilder.addAttachments(message: Notify): EmailPopulatingBuilder {
        // Firstly see if deprecated DOCUMENT_ID populated
        val documentId = message.documentId
        if (documentId != null) {
            val fileContents = fileStorageClient.getFileContents(documentId)
            fileContents?.let {
                addAttachment(it)
            }
        }

        // Next check for attachments in NOTIFY_ATTACHMENT for notifyId
        val attachments = db.getRange(NotifyAttachment.byNotifyIdFileStorageId(message.notifyId)).toList()
        attachments.forEachUniqueStorageId(fileStorageClient) { addAttachment(it) }

        return this
    }

    private fun EmailPopulatingBuilder.addAttachment(fileContentResult: FileContentReply): EmailPopulatingBuilder {
        val fileName = fileContentResult.fileName
        val fileExtension = fileName.getFileExtension()
        return withAttachment(
            fileName,
            fileContentResult.fileContent,
            MimeTypeMap.getMimeType(fileExtension)
        )
    }

    private fun String.getFileExtension() = this.split('.').last()

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(EmailBuilder::class.java)
    }
}
