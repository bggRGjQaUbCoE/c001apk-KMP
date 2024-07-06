package ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp

/**
 * Created by bggRGjQaUbCoE on 2024/7/2
 */
@Composable
fun CreateFeedDialog(
    title: String = "发布动态",
    prefix: String? = null,
    onDismiss: () -> Unit,
    onPostCreateFeed: (String) -> Unit,
) {
    val focusRequest = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            focusRequest.requestFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    var textInput by remember {
        mutableStateOf(
            TextFieldValue(
                text = prefix.orEmpty(),
                selection = TextRange(prefix?.length ?: 0)
            )
        )
    }
    val textStyle = LocalTextStyle.current
    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }) {
                Text(text = "Cancel")
            }
        },
        confirmButton = {
            TextButton(
                enabled = textInput.text.trim().isNotEmpty(),
                onClick = {
                    onPostCreateFeed(textInput.text)
                }) {
                Text(text = "Publish")
            }
        },
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
            )
        },
        text = {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequest),
                minLines = 4,
                maxLines = 8,
                value = textInput,
                textStyle = textStyle.copy(fontSize = 18.sp),
                onValueChange = {
                    if (it.text.length <= 500)
                        textInput = it
                }
            )
        }
    )
}