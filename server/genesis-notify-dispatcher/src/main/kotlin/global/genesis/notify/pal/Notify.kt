package global.genesis.notify.pal

@DslMarker
annotation class NotifyMarker

@NotifyMarker
interface NotifyPal {

    /**
     * Entry point for Notify definition.
     *
     * For more information see
     * * [NotifyBuilder]
     * * [NotifyBuilder.symphony]
     * * [NotifyBuilder.email]
     */
    @NotifyMarker
    fun notify(init: NotifyBuilder.() -> Unit): NotifyBuilder
}

/**
 * The Notify Server top level configuration.
 *
 * The following options are available
 * * [symphony] define a symphony connection
 * * [email] define an email server connection
 */
@NotifyMarker
interface NotifyBuilder {
    @NotifyMarker
    fun gateways(init: GatewaysBuilder.() -> Unit)
}
