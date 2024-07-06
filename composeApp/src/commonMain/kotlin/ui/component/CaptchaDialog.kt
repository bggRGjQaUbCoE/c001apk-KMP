package ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.sp
import constant.Constants.EMPTY_STRING

/**
 * Created by bggRGjQaUbCoE on 2024/7/6
 */
@Composable
fun CaptchaDialog(
    image: ImageBitmap,
    onDismiss: () -> Unit,
    onValidateCaptcha: (String) -> Unit,
) {
    val focusRequest = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            focusRequest.requestFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    var textInput by remember { mutableStateOf(EMPTY_STRING) }
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
                onClick = {
                    onDismiss()
                    onValidateCaptcha(textInput)
                }) {
                Text(text = "OK")
            }
        },
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Captcha",
            )
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    bitmap = image,
                    contentDescription = null,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequest),
                    maxLines = 1,
                    value = textInput,
                    textStyle = textStyle.copy(fontSize = 18.sp),
                    onValueChange = {
                        if (it.length <= 4)
                            textInput = it
                    }
                )
            }
        }
    )
}