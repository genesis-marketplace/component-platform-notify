package global.genesis.notify.pal.parse

import global.genesis.commons.config.GenesisConfigurationException
import global.genesis.notify.gateway.email.EmailGatewayConfig
import global.genesis.notify.pal.NotifyBuilder
import global.genesis.notify.pal.NotifyDefinition
import global.genesis.notify.pal.NotifyScript
import org.apache.commons.collections.CollectionUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.simplejavamail.api.mailer.config.TransportStrategy
import kotlin.test.assertTrue

class NotifyDefinitionTest {

    @Test
    fun test() {
        var result: NotifyBuilder? = null

        runScript {
            result = notify {
                gateways {
                    email(id = "email1") {
                        smtpUser = "test3@email.com"
                        smtpHost = ""
                        smtpPw = ""
                        systemDefaultEmail = "abc@email.com"
                        systemDefaultUserName = "Abc"
                        smtpProtocol = TransportStrategy.SMTPS
                    }
                }
            }
        }

        val definition = (result as NotifyDefinition)

        val configById = definition.gatewayConfigs.configById
        val email1 = configById["email1"]
        assertThat((email1 as EmailGatewayConfig).smtpUser).isEqualTo("test3@email.com")
        assertThat(email1.systemDefaultUserName).isEqualTo("Abc")
        assertThat(email1.systemDefaultEmail).isEqualTo("abc@email.com")
    }

    @Test
    fun test_error_accumulation() {
        var result: NotifyBuilder? = null
        try {
            runScript {
                result = notify {
                    gateways {
                        email("email1") {
                            smtpUser = "test3@email.com"
                            smtpProtocol = TransportStrategy.SMTPS
                        }
                        email("email2") {
                            smtpUser = "test3@email.com"
                            smtpProtocol = TransportStrategy.SMTPS
                        }

                        email("email3") {
                            smtpUser = "test3@email.com"
                        }
                    }
                }
            }
            (result as NotifyDefinition).gatewayConfigs.validate()
        } catch (ex: GenesisConfigurationException) {
            val list = listOf(
                "email1; smtpHost is a required Parameter",
                "email1; smtpPw is a required Parameter",
                "email1; systemDefaultEmail is a required Parameter",
                "email1; systemDefaultUserName is a required Parameter",
                "email2; smtpHost is a required Parameter",
                "email2; smtpPw is a required Parameter",
                "email2; systemDefaultEmail is a required Parameter",
                "email2; systemDefaultUserName is a required Parameter",
                "email3; smtpHost is a required Parameter",
                "email3; smtpPw is a required Parameter",
                "email3; systemDefaultEmail is a required Parameter",
                "email3; systemDefaultUserName is a required Parameter"
            )
            val errorList = (result as NotifyDefinition).gatewayConfigs.errorList
            assertTrue(CollectionUtils.isEqualCollection(list, errorList))
        }
    }

    private fun runScript(init: NotifyScript.() -> Unit) {
        val builder = NotifyScript()
        builder.init()
    }
}
