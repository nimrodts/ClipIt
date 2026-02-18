# AGENTS.md - Agent Coding Guidelines for ClipIt

This file provides guidelines for agentic coding agents operating in this repository.

## Project Overview

ClipIt is an Android coupon management app built with:
- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Database**: Room (SQLite)
- **Architecture**: MVVM with Unidirectional Data Flow
- **AI**: Google Gemini API
- **Build**: Gradle (Kotlin DSL)

---

## Build Commands

```bash
./gradlew assembleDebug      # Build debug APK
./gradlew assembleRelease    # Build release APK
./gradlew build              # Build and run all checks
./gradlew clean              # Clean build directory
./gradlew installDebug       # Build and install debug APK
```

---

## Test Commands

```bash
./gradlew test               # Run all unit tests
./gradlew testDebugUnitTest  # Run debug unit tests only
./gradlew connectedDebugAndroidTest  # Run instrumented tests
```

### Run Single Test
```bash
# Single test class
./gradlew test --tests "com.nimroddayan.clipit.data.model.CouponTest"
# Single test method
./gradlew test --tests "com.nimroddayan.clipit.data.model.CouponTest.default values are set correctly"
# Instrumented test
./gradlew connectedDebugAndroidTest --tests "com.nimroddayan.clipit.data.CouponRepositoryTest.insert_createsHistoryEntry"
```

### Test Locations
- Unit tests: `app/src/test/java/`
- Instrumented tests: `app/src/androidTest/java/`

---

## Lint Commands

```bash
./gradlew lint               # Run lint on default variant
./gradlew lintDebug          # Run lint on debug variant
./gradlew lintFix           # Run lint and apply safe fixes
./gradlew lintAnalyzeDebug  # Analyze lint without reporting
```

---

## Code Style Guidelines

### General Formatting
- **Indentation**: 4 spaces (no tabs)
- **Line length**: Soft limit 120 characters
- **Blank lines**: Single between declarations, double between functions
- **Braces**: K&R style (opening brace on same line)

### Naming Conventions
- **Classes**: PascalCase (e.g., `CouponRepository`)
- **Functions/Variables**: camelCase (e.g., `insertCoupon`, `couponDao`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_VALUE`)
- **Packages**: lowercase (e.g., `com.nimroddayan.clipit.data`)

### Import Organization
Order: 1) kotlin.*, 2) androidx.*, 3) com.*/org.*, 4) com.nimroddayan.clipit.*  
Alphabetical within each group with blank line between groups.

### Data Classes (Models)
Use `data class` with Room annotations. Add `@Serializable` for Kotlin serialization.
```kotlin
@Serializable
@Entity(
    foreignKeys = [ForeignKey(entity = Category::class, parentColumns = ["id"], childColumns = ["categoryId"], onDelete = ForeignKey.SET_NULL)],
    indices = [Index(value = ["categoryId"])]
)
data class Coupon(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, val currentValue: Double, val initialValue: Double,
    val expirationDate: Long, val categoryId: Long?,
)
```

### ViewModels
Extend `ViewModel` from `androidx.lifecycle`. Use `StateFlow` for UI state, `MutableStateFlow` for private mutable state, and `viewModelScope` for coroutine scope.
```kotlin
class CouponViewModel(private val couponRepository: CouponRepository) : ViewModel() {
    val allCoupons: Flow<List<Coupon>> = couponRepository.allCoupons
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    fun insert(coupon: Coupon) {
        viewModelScope.launch {
            _isLoading.value = true
            try { couponRepository.insert(coupon) } finally { _isLoading.value = false }
        }
    }
}
```

### Repository Pattern
Place in `data/` directory. Inject DAOs via constructor. Use `suspend` functions for database operations. Return `Flow` for reactive data streams.

### Coroutines
Use `viewModelScope.launch` in ViewModels. Use `Dispatchers.IO` for database operations. Handle exceptions with try-catch.

### Error Handling
Create custom exceptions for domain-specific errors. Catch exceptions in ViewModels and expose via StateFlow.
```kotlin
class DuplicateRedeemCodeException(message: String) : Exception(message)
```

### Testing Patterns

#### Unit Tests (JUnit 4)
```kotlin
class CouponTest {
    @Test
    fun `default values are set correctly`() {
        val coupon = Coupon(name = "Test", currentValue = 100.0, initialValue = 100.0,
            expirationDate = 1700000000000L, categoryId = null)
        assertFalse(coupon.isArchived)
        assertNull(coupon.redeemCode)
    }
}
```

#### Instrumented Tests (Room)
Use `AndroidJUnit4` runner. Use `Room.inMemoryDatabaseBuilder` for testing. Use `runTest` from `kotlinx.coroutines.test`. Clean up in `@After`.

---

## Existing Agent Rules

From `.agent/rules/`:
- **build-check.md**: After each operation, build to check if code works
- **after-change-build.md**: After changes, verify build still works; fix errors
- **agent-logs.md**: Create debug logs under `AgentLogs` folder
- **utils.md**: Create debugging scripts inside `AgentUtils` folder

---

## Common Tasks Reference

| Task | Command |
|------|---------|
| Build debug APK | `./gradlew assembleDebug` |
| Run unit tests | `./gradlew test` |
| Run single test | `./gradlew test --tests "ClassName.methodName"` |
| Run lint | `./gradlew lint` |
| Fix lint | `./gradlew lintFix` |
| Clean build | `./gradlew clean` |
| Install on device | `./gradlew installDebug` |
