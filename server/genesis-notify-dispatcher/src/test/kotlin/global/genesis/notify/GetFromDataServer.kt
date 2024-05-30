package global.genesis.notify

import global.genesis.commons.model.GenesisSet
import global.genesis.net.GenesisMessageClient
import global.genesis.net.channel.GenesisChannel
import org.slf4j.LoggerFactory
import java.util.ArrayList
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class GetFromDataServer {

    companion object {

        private val logger = LoggerFactory.getLogger(GetFromDataServer::class.java)

        fun retrieveDataServerQueryData(
            messageClient: GenesisMessageClient,
            datasource: String,
            criteria: String?,
            userName: String?
        ): Future<List<GenesisSet>?> {
            val fut = CompletableFuture<List<GenesisSet>>()
            val sourceRef = UUID.randomUUID().toString()

            if (!messageClient.isConnected) {
                logger.error("Failed to send dataserver request for {}", datasource)
                fut.completeExceptionally(Exception("DataServer not Active"))
                return fut
            }
            val listOfRows: MutableList<GenesisSet> = ArrayList()
            messageClient.handler.addListener { replySet: GenesisSet, channel: GenesisChannel ->
                when (replySet.getString("MESSAGE_TYPE")) {
                    "LOGON_ACK", "MORE_ROWS_ACK" -> {
                    }
                    "LOGON_NACK", "MSG_NACK" -> {
                        logger.error(
                            "Received NACK from {}:\n{}",
                            datasource,
                            replySet
                        )
                        fut.cancel(true)
                    }
                    "LOGOFF_ACK" -> try {
                        messageClient.shutdown()
                    } catch (e: InterruptedException) {
                        logger.error("Unable to close dataserver connection")
                    }
                    "QUERY_UPDATE" -> {
                        val rowArr: List<GenesisSet?>? =
                            replySet.getArray("ROW", GenesisSet::class.java)
                        if (rowArr != null && rowArr.isNotEmpty()) {
                            for (row in rowArr) {
                                if (row != null) {
                                    listOfRows.add(row)
                                }
                            }
                            val moreRowsSet = GenesisSet()
                            moreRowsSet.setString("MESSAGE_TYPE", "MORE_ROWS")
                            moreRowsSet.setString("SOURCE_REF", sourceRef)
                            moreRowsSet.setDirect("DETAILS", GenesisSet())
                            channel.writeAndFlush(moreRowsSet)
                        } else {
                            fut.complete(listOfRows)
                            val logoffSet = GenesisSet()
                            logoffSet.setString("MESSAGE_TYPE", "DATA_LOGOFF")
                            logoffSet.setString("SOURCE_REF", sourceRef)
                            channel.writeAndFlush(logoffSet)
                        }
                    }
                    else -> {
                        logger.error(
                            "Received unexpected message:\n{}",
                            replySet
                        )
                        fut.cancel(true)
                    }
                }
            }
            val logon = GenesisSet()
            val logonDetails = GenesisSet()
            logon.setString("MESSAGE_TYPE", "DATA_LOGON")
            logon.setString(
                "USER_NAME",
                userName
            )
            logon.setString("SOURCE_REF", sourceRef)
            logonDetails.setString("DATASOURCE_NAME", datasource)
            logonDetails.setInteger("MAX_ROWS", 2500)
            logonDetails.setInteger("MAX_VIEW", 2500)
            logonDetails.setString("CRITERIA_MATCH", criteria)
            logon.setGenesisSet("DETAILS", logonDetails)
            messageClient.sendMessage(logon)
            return fut
        }
    }
}
