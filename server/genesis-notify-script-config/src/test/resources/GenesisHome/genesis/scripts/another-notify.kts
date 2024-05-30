import com.google.inject.Injector
import global.genesis.notify.gateway.Gateway
import global.genesis.notify.gateway.log.LoggerGateway
import global.genesis.notify.message.request.RouteInfo
import global.genesis.notify.pal.GatewayConfig
import org.slf4j.event.Level

notify {
    gateways {
        email(id = "email1") {
            smtpHost = "localhost"
            smtpPort = 10000
            smtpUser = "default@genesis.global"
            smtpPw = "xxxxxxxxxxx"
            smtpProtocol = TransportStrategy.SMTP
            systemDefaultUserName = "Genesis System"
            systemDefaultEmail = "genesis@global.com"
        }

        email(id = "email2") {
            smtpHost = "smtp.office365.com"
            smtpPort = 587
            smtpUser = "default@genesis.global"
            smtpPw = "xxxxxxxxxxx"
            smtpProtocol = TransportStrategy.SMTP_TLS
            systemDefaultUserName = "Genesis System"
            systemDefaultEmail = "genesis@global.com"
        }

        log(id = "logger") {
            defaultLevel = Level.DEBUG
        }

        screen("screen")

        teams("teams") {
            url = "https://some-teams-server-somewhere"
        }

        registerGateway("inline logger", object : GatewayConfig {
            override val id = "CUSTOM"

            override fun validate(): List<String> {
                return emptyList()
            }

            override fun build(injector: Injector): Gateway {
                return LoggerGateway(Level.DEBUG)
            }

            override val routeInfoSet = setOf(RouteInfo("", "", "", "", ""))
        })
    }
}
