package global.genesis.notify.router

import global.genesis.config.system.SystemDefinitionService
import global.genesis.db.rx.RxDb
import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.User
import global.genesis.gen.dao.enums.EntityIdType
import global.genesis.gen.view.entity.ScreenRoute
import global.genesis.session.AuthCacheFactory
import global.genesis.session.ProfileUserCache
import global.genesis.session.ReadOnlyAuthCache
import global.genesis.session.RightSummaryCache
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class UserNameResolverTest {
    private lateinit var profileUserCache: ProfileUserCache
    private lateinit var entityDb: AsyncEntityDb
    private lateinit var rxDb: RxDb
    private lateinit var rightSummaryCache: RightSummaryCache
    private lateinit var systemDefinitionService: SystemDefinitionService
    private lateinit var userNameResolver: UserNameResolver
    private lateinit var authCacheFactory: AuthCacheFactory

    @BeforeEach
    fun setUp() {
        val users = flowOf(
            user("JohnDoe"),
            user("JaneDoe"),
            user("JimmyDoe")
        )

        profileUserCache = mock<ProfileUserCache> {}
        entityDb = mock<AsyncEntityDb> {
            onBlocking { getBulk(eq(User::class)) } doReturn users
        }
        rxDb = mock<RxDb> {}
        rightSummaryCache = mock<RightSummaryCache> {}
        systemDefinitionService = mock<SystemDefinitionService> {}

        val mockAuthCache = mock<ReadOnlyAuthCache> {
            on { getAuthorisedUsers(eq("456")) } doReturn setOf("JohnDoe")
        }

        authCacheFactory = mock<AuthCacheFactory> {
            on { newReader(eq("TestCache")) } doReturn mockAuthCache
        }

        userNameResolver = UserNameResolver(
            profileUserCache = profileUserCache,
            entityDb = entityDb,
            rxDb = rxDb,
            rightSummaryCache = rightSummaryCache,
            systemDefinitionService = systemDefinitionService,
            authCacheFactory = authCacheFactory
        )
    }

    @Test
    fun `Notify Sender is excluded from recipient list if route is configured to exclude sender`() = runBlocking {
        val testNotify = Notify {
            topic = "test"
            body = "test"
            header = "test"
            sender = "JohnDoe"
        }

        val users = userNameResolver.getMatchedGatewayUsers(
            testNotify,
            setOf(mock<ScreenRoute> {}),
            { UserRouteData("123", EntityIdType.ALL, null, true) },
            { UserPermissionData() }
        )

        assertFalse("JohnDoe" in users)
        assertTrue("JaneDoe" in users)
        assertTrue("JimmyDoe" in users)
    }

    @Test
    fun `User receives alert if target is ALL and they have the requisite permission code`() = runBlocking {
        whenever(rightSummaryCache.userHasRight(eq("JohnDoe"), eq("TestCode"))) doReturn true
        whenever(rightSummaryCache.userHasRight(eq("JaneDoe"), eq("TestCode"))) doReturn false
        whenever(rightSummaryCache.userHasRight(eq("JimmyDoe"), eq("TestCode"))) doReturn false

        val testNotify = Notify {
            topic = "test"
            body = "test"
            header = "test"
        }

        val users = userNameResolver.getMatchedGatewayUsers(
            testNotify,
            setOf(mock<ScreenRoute> {}),
            { UserRouteData("123", EntityIdType.ALL, null, false) },
            { UserPermissionData(rightCode = "TestCode") }
        )

        assertTrue("JohnDoe" in users)
        assertFalse("JaneDoe" in users)
        assertFalse("JimmyDoe" in users)
    }

    @Test
    fun `User receives alert if they have visibility on the linked entity in the specified auth cache`() = runBlocking {
        val testNotify = Notify {
            topic = "test"
            body = "test"
            header = "test"
            permissioningEntityId = "456"
        }

        val users = userNameResolver.getMatchedGatewayUsers(
            testNotify,
            setOf(mock<ScreenRoute> {}),
            { UserRouteData("123", EntityIdType.ALL, null, false) },
            { UserPermissionData(authCacheName = "TestCache") }
        )

        assertTrue("JohnDoe" in users)
        assertFalse("JaneDoe" in users)
        assertFalse("JimmyDoe" in users)
    }

    private fun user(user: String) = User {
        userName = user
        status = "ENABLED"
    }
}
