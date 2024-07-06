package ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import openInBrowser
import ui.component.BackButton
import ui.component.settings.BasicListItem

/**
 * Created by bggRGjQaUbCoE on 2024/6/2
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(
    onBackClick: () -> Unit
) {

    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    BackButton {
                        onBackClick()
                    }
                },
                title = {
                    Text(text = "Open Source License")
                },
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateLeftPadding(layoutDirection),
                    end = paddingValues.calculateRightPadding(layoutDirection),
                ),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding())
        ) {
            items(
                items = licenseList,
                key = { it.url }
            ) { item ->
                LicenseRow(item = item)
            }
        }
    }

}

private val licenseList = listOf(
    LicenseItem(
        "Google",
        "Jetpack Compose",
        "https://github.com/androidx/androidx",
        LicenseType.Apache2
    ),
    LicenseItem(
        "JetBrains",
        "Kotlin",
        "https://github.com/JetBrains/kotlin",
        LicenseType.Apache2
    ),
    LicenseItem(
        "JetBrains",
        "compose-multiplatform",
        "https://github.com/JetBrains/compose-multiplatform",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "Material Design 3",
        "https://m3.material.io/",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Google",
        "Material Icons",
        "https://github.com/google/material-design-icons",
        LicenseType.Apache2
    ),
    LicenseItem(
        "ktorio",
        "ktor",
        "https://github.com/ktorio/ktor",
        LicenseType.Apache2
    ),
    LicenseItem(
        "InsertKoinIO",
        "koin",
        "https://github.com/InsertKoinIO/koin",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Kamel-Media",
        "Kamel",
        "https://github.com/Kamel-Media/Kamel",
        LicenseType.Apache2
    ),
    LicenseItem(
        "dokar3",
        "compose-sonner",
        "https://github.com/dokar3/compose-sonner",
        LicenseType.Apache2
    ),
    LicenseItem(
        "cbeyls",
        "HtmlConverterCompose",
        "https://github.com/cbeyls/HtmlConverterCompose",
        LicenseType.Apache2
    ),
    LicenseItem(
        "Lavmee",
        "constraintlayout-compose-multiplatform",
        "https://github.com/Lavmee/constraintlayout-compose-multiplatform",
        LicenseType.Apache2
    ),
    LicenseItem(
        "chrisbanes",
        "material3-windowsizeclass-multiplatform",
        "https://github.com/chrisbanes/material3-windowsizeclass-multiplatform",
        LicenseType.Apache2
    ),
    LicenseItem(
        "jeremyh",
        "jBCrypt",
        "https://github.com/jeremyh/jBCrypt",
        LicenseType.Apache2
    ),
    LicenseItem(
        "jhy",
        "jsoup",
        "https://github.com/jhy/jsoup",
        LicenseType.MIT
    ),
    LicenseItem(
        "onebone",
        "compose-collapsing-toolbar",
        "https://github.com/onebone/compose-collapsing-toolbar",
        LicenseType.MIT
    ),
    LicenseItem(
        "jordond",
        "MaterialKolor",
        "https://github.com/jordond/MaterialKolor",
        LicenseType.MIT
    ),
    LicenseItem(
        "arkivanov",
        "Decompose",
        "https://github.com/arkivanov/Decompose",
        LicenseType.Apache2
    ),
    LicenseItem(
        "gmazzo",
        "gradle-buildconfig-plugin",
        "https://github.com/gmazzo/gradle-buildconfig-plugin",
        LicenseType.MIT
    ),
    LicenseItem(
        "saket",
        "telephoto",
        "https://github.com/saket/telephoto",
        LicenseType.Apache2
    ),
)

data class LicenseItem(
    val author: String,
    val name: String,
    val url: String,
    val type: LicenseType
)

enum class LicenseType {
    Apache2,
    MIT,
    GPL3
}

private fun getLicense(type: LicenseType): String =
    when (type) {
        LicenseType.Apache2 -> "Apache Software License 2.0"
        LicenseType.MIT -> "MIT License"
        LicenseType.GPL3 -> "GNU general public license Version 3"
    }

@Composable
fun LicenseRow(item: LicenseItem) {
    BasicListItem(
        headlineText = "${item.name} - ${item.author}",
        supportingText = "${item.url}\n${getLicense(item.type)}"
    ) {
        openInBrowser(item.url)
    }
}