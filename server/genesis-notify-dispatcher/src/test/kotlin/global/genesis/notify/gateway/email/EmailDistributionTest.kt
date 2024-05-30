package global.genesis.notify.gateway.email

import global.genesis.gen.view.entity.EmailDistributionRoute
import global.genesis.notify.gateway.common.RouteUtils.getEmailDistribution
import global.genesis.notify.message.common.EmailDistribution
import global.genesis.notify.message.common.splitIntoDisplayNameAndAddress
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val JASON = "Jason<jason@email.com>"
private const val BOB = "Bob <bob@email.com>"
private const val TOM = "Tom <tom@email.com>"
private const val CAROL = "carol@email.com"
private const val SUSAN = "susan@email.com"

internal class EmailDistributionTest {
    @Test
    fun testCombinedDistribution() {
        val dist1 = EmailDistribution(
            to = listOf(JASON, BOB),
            cc = listOf(TOM),
            bcc = emptyList()
        )

        val dist2 = EmailDistribution(
            to = emptyList(),
            cc = listOf(CAROL),
            bcc = listOf(SUSAN)
        )

        val combined = dist1 + dist2

        assertThat(combined.to).containsExactly(JASON, BOB)
        assertThat(combined.cc).containsExactly(TOM, CAROL)
        assertThat(combined.bcc).containsExactly(SUSAN)
    }

    @Test
    fun testDistributionWithEmptyOrNullFields() {
        val emailRoute = EmailDistributionRoute(
            emailTo = "jason@email.com,bob@email.com",
            emailCc = null,
            emailBcc = "",
            gatewayId = "",
            topicMatch = "",
            notifyRouteId = ""
        )
        val emailDistribution = emailRoute.getEmailDistribution()

        assertThat(emailDistribution.to).containsExactly("jason@email.com", "bob@email.com")
        assertThat(emailDistribution.cc).isEmpty()
        assertThat(emailDistribution.bcc).isEmpty()
    }

    @Test
    fun testSplitIntoDisplayNameAndAddress() {
        assert(JASON.splitIntoDisplayNameAndAddress() == "Jason" to "jason@email.com")
        assert(BOB.splitIntoDisplayNameAndAddress() == "Bob" to "bob@email.com")
        assert(CAROL.splitIntoDisplayNameAndAddress() == "" to "carol@email.com")
    }
}
