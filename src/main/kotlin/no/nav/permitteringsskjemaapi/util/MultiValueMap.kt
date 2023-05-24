package no.nav.permitteringsskjemaapi.util

import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap


fun <K : Any, V>multiValueMapOf(vararg bindings: Pair<K, V>): MultiValueMap<K, V> =
    LinkedMultiValueMap<K, V>().apply {
        bindings.forEach { (key, value) ->
            add(key, value)
        }
    }