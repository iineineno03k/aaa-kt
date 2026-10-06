package io.github.iineineno03k.aaa

/**
 * Starts a test with the Arrange phase.
 *
 * Whatever [block] returns becomes the receiver of the following [Arranged.act] and [Acted.assert]
 * blocks, so returning an anonymous object exposes several values by name without declaring a class.
 *
 * ```
 * arrange {
 *     object {
 *         val user = User(name = "Test User", age = 30)
 *         val permissions = listOf("read", "write")
 *     }
 * }.act {
 *     formatUser(user, permissions)
 * }.assert { result ->
 *     assertEquals("Test User (30)", result.displayName)
 *     assertEquals(permissions, result.permissions)
 * }
 * ```
 */
public inline fun <A> arrange(block: () -> A): Arranged<A> = Arranged(block())

/**
 * Starts a test with the Act phase, for tests that need no arrangement.
 *
 * ```
 * act { add(1, 2) }.assert { result -> assertEquals(3, result) }
 * ```
 */
public inline fun <R> act(block: () -> R): Acted<Unit, R> = Acted(Unit, block())

/** The outcome of the Arrange phase. The only way forward is [act]. */
public class Arranged<out A>
    @PublishedApi
    internal constructor(
        @PublishedApi internal val arranged: A,
    ) {
        /** Runs the code under test once, with the arranged value as the receiver. */
        public inline fun <R> act(block: A.() -> R): Acted<A, R> = Acted(arranged, arranged.block())
    }

/** The outcome of the Act phase. The only way forward is [assert]. */
public class Acted<out A, out R>
    @PublishedApi
    internal constructor(
        @PublishedApi internal val arranged: A,
        @PublishedApi internal val result: R,
    ) {
        /** Verifies the result of the Act phase, with the arranged value as the receiver. */
        public inline fun assert(block: A.(result: R) -> Unit) {
            arranged.block(result)
        }
    }
