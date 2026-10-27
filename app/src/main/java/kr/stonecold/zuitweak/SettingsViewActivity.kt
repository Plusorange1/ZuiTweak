package kr.stonecold.zuitweak

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.stonecold.zuitweak.common.*
import kr.stonecold.zuitweak.ui.theme.ZuiTweakTheme
import java.util.Locale

class SettingsViewActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val language = try {
            LanguageUtil.resolveLanguage()
        } catch (e: Exception) {
            "en"
        }
        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.setLocale(Locale(language))
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZuiTweakTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CacheSettingsScreen()
                }
            }
        }
    }

    private fun getAllSettings(): String {
        val stringBuilder = StringBuilder()
        SharedPrefsUtil.init()

        if (!SharedPrefsUtil.isInitialized) {
            stringBuilder.append(getString(R.string.settings_not_initialized))
        } else {
            val allEntries = SharedPrefsUtil.getAllOptions()
            for ((key, value) in allEntries) {
                stringBuilder.append("$key = $value\n")
            }
        }
        return stringBuilder.toString()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CacheSettingsScreen() {
        var settingsText by remember { mutableStateOf(getString(R.string.settings_view_placeholder)) }
        val coroutineScope = rememberCoroutineScope()

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.settings_view)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        coroutineScope.launch {
                            settingsText = withContext(Dispatchers.IO) { getAllSettings() }
                        }
                    }) {
                        Text(stringResource(R.string.load_settings))
                    }
                    OutlinedButton(onClick = {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) { SharedPrefsUtil.clearAllOptions() }
                            settingsText = getString(R.string.settings_cleared)
                            ActivityCompat.finishAffinity(this@SettingsViewActivity)
                        }
                    }) {
                        Text(stringResource(R.string.clear_settings))
                    }
                }

                Text(
                    text = settingsText,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 500,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}
