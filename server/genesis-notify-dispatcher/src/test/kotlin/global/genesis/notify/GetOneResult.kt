package global.genesis.notify

import global.genesis.db.rx.AbstractEntityBulkTableSubscriber
import global.genesis.db.rx.entity.multi.AsyncEntityDb
import global.genesis.gen.dao.Notify
import global.genesis.gen.dao.description.NotifyDescription
import java.util.concurrent.CountDownLatch

class GetOneResult(val topic: String, db: AsyncEntityDb) : AbstractEntityBulkTableSubscriber<Notify>(
    db,
    NotifyDescription
) {

    private val latch = CountDownLatch(1)
    private var result: Notify? = null

    init {
        initialise()
    }

    fun get(): Notify {
        latch.await()
        return result!!
    }

    override fun onInsert(record: Notify) {
        if (record.topic == topic) {
            result = record
            latch.countDown()
        }
    }
}
