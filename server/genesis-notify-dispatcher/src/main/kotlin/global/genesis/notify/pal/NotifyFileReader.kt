package global.genesis.notify.pal

import global.genesis.commons.annotation.ProviderOf
import global.genesis.pal.reader.AbstractPalScriptReader
import groovy.lang.Singleton
import javax.inject.Provider

@Singleton
@ProviderOf(type = NotifyDefinition::class)
class NotifyFileReader : AbstractPalScriptReader("-notify.kts"), Provider<NotifyDefinition> {
    override fun get(): NotifyDefinition = loadKtsFiles<NotifyDefinition>().first()
}
