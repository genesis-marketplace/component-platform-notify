package global.genesis.notify.message.common

import global.genesis.notify.message.common.EmailDistribution.Companion.splittingRegex

data class EmailDistribution @JvmOverloads constructor(
    var to: List<String> = emptyList(),
    var cc: List<String> = emptyList(),
    var bcc: List<String> = emptyList()
) {
    constructor(
        to: String
    ) : this(to = listOf(to))

    operator fun plus(other: EmailDistribution): EmailDistribution {
        return EmailDistribution(
            to + other.to,
            cc + other.cc,
            bcc + other.bcc
        )
    }

    fun hasRecipients(): Boolean {
        return to.isNotEmpty() || cc.isNotEmpty() || bcc.isNotEmpty()
    }

    companion object {
        val splittingRegex = "^\\s*(.*?)(?:\\s*<\\s*(.*?)\\s*>)?$".toRegex()
    }
}

fun String.splitIntoDisplayNameAndAddress(): Pair<String?, String> {
    val matchResult = splittingRegex.matchEntire(this)
        ?: throw IllegalArgumentException("Invalid email provided \"$this\"")

    val firstMatch = matchResult.groupValues[1]

    return when (val secondMatch = matchResult.groupValues[2]) {
        "" -> "" to firstMatch
        else -> firstMatch to secondMatch
    }
}
