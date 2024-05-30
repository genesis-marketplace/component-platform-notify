package global.genesis.notify.pal

import global.genesis.pal.shared.genesisApplicationScriptConfig
import global.genesis.pal.shared.genesisEvalApplicationScriptConfig
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.defaultImports

@KotlinScript(
    displayName = "Genesis Notify Config",
    filePathPattern = ".*-notify.kts",
    compilationConfiguration = NotifyScriptConfig::class,
    evaluationConfiguration = NotifyScriptEvalConfig::class
)
open class NotifyScript : NotifyPal {
    override fun notify(init: NotifyBuilder.() -> Unit): NotifyBuilder {
        val builder = NotifyDefinition()
        builder.init()
        return builder
    }
}

object NotifyScriptEvalConfig :
    ScriptEvaluationConfiguration(genesisEvalApplicationScriptConfig(sysDefReceiver = true))

object NotifyScriptConfig : ScriptCompilationConfiguration(
    genesisApplicationScriptConfig(sysDefReceiver = true) {
        defaultImports("org.simplejavamail.api.mailer.config.TransportStrategy")
        defaultImports("global.genesis.notify.pal.extn.*")
    }
)
