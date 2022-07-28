package com.example.composeinput

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.molecule.RecompositionClock.ContextClock
import app.cash.molecule.RecompositionClock.Immediate
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.flow.MutableSharedFlow

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {

      var useImmediateClock by remember { mutableStateOf(false) }
      val scope = rememberCoroutineScope()
      val events = remember { MutableSharedFlow<String>(extraBufferCapacity = 1) }
      val state: String = remember(useImmediateClock) {
        scope.launchMolecule(if (useImmediateClock) Immediate else ContextClock) {
          events.collectAsState(initial = "").value
        }
      }.collectAsState().value

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
          OutlinedTextField(
            value = state,
            onValueChange = { events.tryEmit(it) },
            modifier = Modifier.fillMaxWidth()
          )
          Text(
            "A reliable way to observe issues is to enter text into the input field, and then hold down backspace to rapidly delete characters."
          )
        }
      }
    }
  }
}