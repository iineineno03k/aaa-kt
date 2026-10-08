# aaa-kt

Arrange-Act-Assert as a typed call chain for Kotlin tests. No dependencies beyond the Kotlin standard library, so it works with JUnit, Kotest, or any other runner.

```kotlin
@Test
fun `formats user details and permissions`() =
    arrange {
        object {
            val user = User(name = "Test User", age = 30)
            val permissions = listOf("read", "write")
        }
    }.act {
        formatUser(user, permissions)
    }.assert { result ->
        assertEquals("Test User (30)", result.displayName)
        assertEquals(permissions, result.permissions)
    }
```

## Why

`// Arrange`, `// Act`, `// Assert` comments are a convention the compiler cannot check. Here the three phases are functions, and each one returns a type that only offers the next phase:

- `arrange { }` returns a value whose only member is `act`.
- `act { }` returns a value whose only member is `assert`.
- Data moves between phases only through the chain: the arranged value is the receiver (`this`) of `act` and `assert`, and the result of `act` is the argument of `assert`.

So a test cannot assert before acting, cannot act twice, and shows what it prepares, what it runs, and what it checks.

## Install

```kotlin
dependencies {
    testImplementation("io.github.iineineno03k:aaa-kt:0.1.1")
}
```

Requires Kotlin 2.0 or later and JVM 17 or later.

## Usage

### Several arranged values

Return an anonymous object from `arrange`. Its properties are available by name in `act` and `assert`, with no `Pair` or data class.

### No arrangement

```kotlin
@Test
fun `adds two numbers`() =
    act { add(1, 2) }.assert { result -> assertEquals(3, result) }
```

### Suspend functions

Every phase is an `inline` function, so suspend functions can be called directly inside `runTest`.

```kotlin
@Test
fun `loads the user`() =
    runTest {
        arrange { repository.save(user) }
            .act { useCase.load(id) }
            .assert { result -> assertEquals(id, result.id) }
    }
```

### Exceptions

Use `actThrows` when the code under test is expected to throw. The exception becomes the argument of `assert`; an exception of another type propagates, and completing normally fails the test.

```kotlin
@Test
fun `rejects a negative amount`() =
    actThrows<IllegalArgumentException> { Money(-1) }
        .assert { exception -> assertEquals("amount must not be negative", exception.message) }
```

It is also available after `arrange`:

```kotlin
arrange { -1 }
    .actThrows<IllegalArgumentException> { Money(this) }
    .assert { exception -> assertEquals("amount must not be negative", exception.message) }
```

## Limitations

- A chain that stops at `act` still compiles. `act` has already run by then, but nothing is verified. With JUnit, write the test as an expression body (`fun test() = arrange { }...`): a chain without `assert` then gives the test function a non-`Unit` return type, which JUnit does not accept as a test method.
- The types enforce the frame, not what goes inside it. Calling an assertion inside `act` is still possible.

## License

MIT
