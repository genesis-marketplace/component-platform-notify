package global.genesis.notify.gateway.sendgrid

import com.google.inject.Inject
import com.sendgrid.Method
import com.sendgrid.Request
import com.sendgrid.helpers.mail.Mail
import com.sendgrid.helpers.mail.objects.Attachments
import com.sendgrid.helpers.mail.objects.Content
import com.sendgrid.helpers.mail.objects.Email
import com.sendgrid.helpers.mail.objects.Personalization
import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.file.message.common.FileContentReply
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.NotifyAttachment
import global.genesis.notify.documents.FileStorageClientWrapper
import global.genesis.notify.documents.MimeTypeMap
import global.genesis.notify.message.common.EmailDistribution
import global.genesis.notify.message.common.splitIntoDisplayNameAndAddress
import global.genesis.session.UserEmailCache
import kotlinx.coroutines.flow.toList
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

class SendGridEmailBuilder @Inject constructor(
    private val config: SendGridGatewayConfig,
    private val emailCache: UserEmailCache,
    private val db: AsyncEntityDb,
    private val fileStorageClient: FileStorageClientWrapper
) {
    suspend fun buildEmail(recipientUserName: String?, message: Notify): Request {
        val email = emailCache.userEmailAddress(recipientUserName)
            ?: throw RuntimeException("Could not obtain email address for user $recipientUserName")
        val emailDistribution = EmailDistribution("$recipientUserName <$email>")
        return buildEmail(emailDistribution, message)
    }

    suspend fun buildEmail(emailDistribution: EmailDistribution, message: Notify): Request {
        val mail = Mail()
        mail.setFrom(Email(config.defaultSender))
        mail.addPersonalization(getDestinations(emailDistribution))
        mail.setSubject(message.header)
        mail.addContent(message)
        mail.addAllAttachments(message)

        val request = Request()
        request.method = Method.POST
        request.endpoint = "mail/send"
        request.body = mail.build()
        return request
    }

    private fun getDestinations(emailDistribution: EmailDistribution): Personalization {
        val personalization = Personalization()

        for (email in emailDistribution.to) {
            val (displayName, emailAddress) = email.splitIntoDisplayNameAndAddress()
            personalization.addTo(Email(emailAddress, displayName))
        }
        for (email in emailDistribution.cc) {
            val (displayName, emailAddress) = email.splitIntoDisplayNameAndAddress()
            personalization.addCc(Email(emailAddress, displayName))
        }
        for (email in emailDistribution.bcc) {
            val (displayName, emailAddress) = email.splitIntoDisplayNameAndAddress()
            personalization.addBcc(Email(emailAddress, displayName))
        }
        return personalization
    }

    private suspend fun Mail.addContent(message: Notify) {
        val documentContentResult = fileStorageClient.generateDocumentContent(message)
        if (documentContentResult.assets.isNotEmpty()) {
            documentContentResult.assets.forEach {
                addEmbeddedImage(it)
            }
        }
        this.addContent(Content("text/html", documentContentResult.content))
    }

    private suspend fun Mail.addAllAttachments(message: Notify) {
        //  Firstly see if deprecated DOCUMENT_ID populated
        if (message.documentId != null) {
            val result = fileStorageClient.getFileContents(message.documentId!!)
            result?.let {
                this.addAttachment(result)
            }
        }

        //  Next check for attachments in NOTIFY_ATTACHMENT for notifyId
        val attachments = db.getRange(NotifyAttachment.byNotifyIdFileStorageId(message.notifyId)).toList()
        attachments.forEachUniqueStorageId(fileStorageClient) { addAttachment(it) }
    }

    private fun Mail.addAttachment(fileContentResult: FileContentReply) {
        attach(fileContentResult, "attachment")
    }

    private fun Mail.addEmbeddedImage(fileContentResult: FileContentReply) {
        attach(fileContentResult, "inline")
    }

    private fun Mail.attach(
        fileContentResult: FileContentReply,
        disposition: String
    ) {
        val attachment = Attachments()
        val fileName = fileContentResult.fileName
        val fileExtension = fileName.split('.').last()
        attachment.content = Base64.getEncoder().encodeToString(fileContentResult.fileContent)
        attachment.filename = fileName
        attachment.type = MimeTypeMap.getMimeType(fileExtension)
        if (disposition == "inline") {
            attachment.contentId = fileName
        }
        attachment.disposition = disposition
        this.addAttachments(attachment)
    }

    companion object {
        val LOG: Logger = LoggerFactory.getLogger(SendGridEmailBuilder::class.java)
    }
}

suspend fun List<NotifyAttachment>.forEachUniqueStorageId(fileStorageClient: FileStorageClientWrapper, consumer: (fileContent: FileContentReply) -> Unit) {
    val fileStorageIds = this.map { it.fileStorageId }.toSet()
    if (fileStorageIds.isNotEmpty()) {
        fileStorageClient.getAllFileContents(fileStorageIds).forEach {
            consumer.invoke(it)
        }
    }
}
