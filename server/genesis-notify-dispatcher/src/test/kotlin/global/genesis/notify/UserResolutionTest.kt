package global.genesis.notify

import global.genesis.commons.model.GenesisSet
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.NotifyAlert
import global.genesis.testsupport.AbstractGenesisTestSupport
import global.genesis.testsupport.GenesisTestConfig
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.awaitility.Awaitility
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserResolutionTest : AbstractGenesisTestSupport<GenesisSet>(
    GenesisTestConfig {
        packageNames = listOf("global.genesis.notify", "global.genesis.file.storage.provider").toMutableList()
        genesisHome = "/genesisHome/"
        initialDataFile = "notify-user-resolution-data.csv"
        scriptFileName = "another-notify.kts"
        useTempClassloader = true
    }
) {
    override fun systemDefinition(): Map<String, Any> = mapOf(
        "IS_SCRIPT" to "true",
        "ADMIN_PERMISSION_ENTITY_FIELD" to "COUNTERPARTY_ID",
        "ADMIN_PERMISSION_ENTITY_TABLE" to "COUNTERPARTY",
        "LOCAL_STORAGE_FOLDER" to "LOCAL_STORAGE",
        "STORAGE_STRATEGY" to "LOCAL"
    )

    @Test
    fun `resolution of self sends to the correct users`(): Unit = runBlocking {
        entityDb.insert(
            Notify {
                topic = "screenSelf"
                sender = "TestUser1"
                header = "Test Notification"
                body = "This message is a a test"
            }
        )

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until {
            rxDb.getBulk("NOTIFY_ALERT").blockingIterable().toList().size == 1
        }
        val alerts = entityDb.getBulk(NotifyAlert::class).toList()

        assertEquals(1, alerts.size)
        assertEquals("TestUser1", alerts[0].userName)
    }

    @Test
    fun `resolution of individual user name sends to the correct users`(): Unit = runBlocking {
        entityDb.insert(
            Notify {
                topic = "screenUser"
                sender = "TestUser1"
                header = "Test Notification"
                body = "This message is a a test"
            }
        )

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until {
            rxDb.getBulk("NOTIFY_ALERT").blockingIterable().toList().size == 1
        }
        val alerts = entityDb.getBulk(NotifyAlert::class).toList()

        assertEquals(1, alerts.size)
        assertEquals("TestUser2", alerts[0].userName)
    }

    @Test
    fun `resolution of profile sends to the correct users`(): Unit = runBlocking {
        entityDb.insert(
            Notify {
                topic = "screenProfile"
                sender = "TestUser1"
                header = "Test Notification"
                body = "This message is a a test"
            }
        )

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until {
            rxDb.getBulk("NOTIFY_ALERT").blockingIterable().toList().size == 2
        }
        val alerts = entityDb.getBulk(NotifyAlert::class).toList()

        val users = alerts.mapNotNull { it.userName }
        assertEquals(2, users.size)
        assertTrue(users.contains("TestUser2"))
        assertTrue(users.contains("TestUser3"))
    }

    @Test
    fun `resolution of admin entity sends to the correct users`(): Unit = runBlocking {
        entityDb.insert(
            Notify {
                topic = "screenEntity"
                sender = "TestUser1"
                header = "Test Notification"
                body = "This message is a a test"
            }
        )

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until {
            rxDb.getBulk("NOTIFY_ALERT").blockingIterable().toList().size == 2
        }
        val alerts = entityDb.getBulk(NotifyAlert::class).toList()

        val users = alerts.mapNotNull { it.userName }
        assertEquals(2, users.size)
        assertTrue(users.contains("TestUser1"))
        assertTrue(users.contains("TestUser3"))
    }

    @Test
    fun `resolution of all users sends to the correct users`(): Unit = runBlocking {
        entityDb.insert(
            Notify {
                topic = "screenAll"
                sender = "TestUser1"
                header = "Test Notification"
                body = "This message is a a test"
            }
        )

        Awaitility.await().atMost(10, TimeUnit.SECONDS).until {
            rxDb.getBulk("NOTIFY_ALERT").blockingIterable().toList().size == 4
        }
        val alerts = entityDb.getBulk(NotifyAlert::class).toList()

        val users = alerts.mapNotNull { it.userName }
        assertEquals(4, users.size)
        assertTrue(users.contains("TestUser1"))
        assertTrue(users.contains("TestUser2"))
        assertTrue(users.contains("TestUser3"))
        assertTrue(users.contains("TestUser4"))
    }
}
