package com.example.composeinput

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text2.BasicTextField2
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.foundation.text2.input.textAsFlow
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.molecule.RecompositionMode
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {

  @OptIn(ExperimentalFoundationApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      var useImmediateClock by remember { mutableStateOf(false) }
      val scope = rememberCoroutineScope()
      val events = remember { MutableSharedFlow<Event>(extraBufferCapacity = 1) }
      val moleculeStateFlow: StateFlow<UiState> = remember(useImmediateClock) {
        scope.launchMolecule(
          if (useImmediateClock) RecompositionMode.Immediate else RecompositionMode.ContextClock,
        ) {
          val textFieldState = remember { TextFieldState("") }
          var someDerivedText by remember { mutableStateOf("") }
          var haveDelay by remember { mutableStateOf(true) }
          LaunchedEffect(events) {
            events.collect { event ->
              when (event) {
                Event.FlipDelaySwitch -> haveDelay = !haveDelay
              }
            }
          }
          LaunchedEffect(Unit) {
            textFieldState.textAsFlow().collect { text ->
              if (haveDelay) {
                delay((200..600L).random())
              }
              someDerivedText = text.map { "$it$it" }.joinToString(separator = "")
            }
          }
          UiState(textFieldState, someDerivedText, haveDelay)
        }
      }
      val uiState: UiState by moleculeStateFlow.collectAsState()

      MaterialTheme {
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(16.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Use immediate clock")
            Switch(
              checked = useImmediateClock,
              onCheckedChange = { useImmediateClock = !useImmediateClock },
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Is delay on: ${uiState.isDelayOn}")
          }
          BasicTextField2(
            state = uiState.textFieldState,
            modifier = Modifier.fillMaxWidth().border(1.dp, Color.Red),
            textStyle = TextStyle.Default.copy(fontSize = 24.sp),
          )
          Text(uiState.someOtherUiState)
          Button(onClick = { events.tryEmit(Event.FlipDelaySwitch) }) {
            Text("Add/Remove delay from someOtherUiState mapping")
          }
        }
      }
    }
  }
}

sealed interface Event {
  data object FlipDelaySwitch : Event
}

@OptIn(ExperimentalFoundationApi::class)
data class UiState(
  val textFieldState: TextFieldState,
  val someOtherUiState: String,
  val isDelayOn: Boolean,
)
