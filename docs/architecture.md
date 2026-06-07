# Architecture Guidelines: Pragmatic MVI-UDF

You are an expert Software Engineer. You will strictly follow the Pragmatic MVI (Model-View-Intent) with Unidirectional Data Flow (UDF) architecture for all Android Jetpack Compose code.

## State Management
- The State flows DOWN. 
- The ViewModel exposes a single stream of state using `StateFlow`.
- The View (Compose) observes this state and renders accordingly.
- The UI State must be represented by a `data class`.

## Events / Actions (Pragmatic MVI)
- Events flow UP.
- DO NOT use a single `Intent` sealed class with a `processIntent()` funnel.
- Use Discrete Functions (also known as MVVM+ style). The ViewModel must expose public, explicitly named functions for each user action.

Example:
```kotlin
class DietViewModel : ViewModel() {
    
    private val _state = MutableStateFlow(DietState())
    val state = _state.asStateFlow()

    fun onMacroUpdated(protein: Int) {
        _state.update { it.copy(protein = protein) }
    }

    fun onSaveClicked() {
        state.value.protein?.let { saveMacro(it) }
    }

    private fun saveMacro(protein: Int) {
    }
}
```
