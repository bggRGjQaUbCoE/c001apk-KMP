package ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.AllInclusive
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Source
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import c001apk_kmp.composeapp.BuildConfig
import c001apk_kmp.composeapp.generated.resources.Res
import c001apk_kmp.composeapp.generated.resources.app_name
import c001apk_kmp.composeapp.generated.resources.ic_launcher_round
import constant.Constants.URL_SOURCE_CODE
import openInBrowser
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.component.BackButton
import ui.component.settings.BasicListItem

/**
 * Created by bggRGjQaUbCoE on 2024/6/2
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    onLicenseClick: () -> Unit,
) {

    val rememberScrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton { onBackClick() }
                },
                title = { Text(text = "About") },
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_launcher_round),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            HorizontalDivider()
            BasicListItem(
                leadingImageVector = Icons.Outlined.AllInclusive,
                headlineText = stringResource(resource = Res.string.app_name),
                supportingText = "test only"
            ) { }
            BasicListItem(
                leadingImageVector = Icons.Default.ErrorOutline,
                headlineText = "Version",
                supportingText = "${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
            ) { }
            BasicListItem(
                leadingImageVector = Icons.Outlined.Code,
                headlineText = "Source Code",
                supportingText = URL_SOURCE_CODE
            ) {
                openInBrowser(URL_SOURCE_CODE)
            }
            BasicListItem(
                leadingImageVector = Icons.Outlined.Source,
                headlineText = "Open Source License"
            ) {
                onLicenseClick()
            }

        }
    }

}