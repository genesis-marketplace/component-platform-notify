package global.genesis.notify.utils

import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

object ConfigValidator {
    fun <T : Any> validateRequiredFields(
        kClass: KClass<T>,
        instance: T,
        vararg optionalFields: String
    ): List<String> {
        val result = mutableListOf<String>()

        kClass.declaredMemberProperties.forEach {
            if (it.get(instance) == null && it.name !in optionalFields) {
                result.add("${it.name} is a required Parameter")
            }
        }
        return result
    }
}
