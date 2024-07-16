package no.nav.permitteringsskjemaapi.journalføring

import org.springframework.test.context.transaction.TestTransaction

private fun <T: Any?> transaction(block: () -> T): T {
    if (!TestTransaction.isActive()) TestTransaction.start()
    val result = block()
    TestTransaction.flagForCommit()
    TestTransaction.end()
    return result
}
