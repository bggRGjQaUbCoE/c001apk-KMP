package ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import util.composeClick

/**
 * Created by bggRGjQaUbCoE on 2024/6/2
 */
@Composable
fun BackButton(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
) {
    IconButton(modifier = modifier, onClick = composeClick { onBackClick() }) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = null
        )
    }
}