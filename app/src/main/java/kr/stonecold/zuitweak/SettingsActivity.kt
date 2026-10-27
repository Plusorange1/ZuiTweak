package kr.stonecold.zuitweak

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getString
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.launch
import kr.stonecold.zuitweak.common.*
import kr.stonecold.zuitweak.ui.theme.ZuiTweakTheme
import java.util.Locale

private lateinit var prefsSnapshot: Map<String, *>

class SettingsActivity : ComponentActivity() {
    private var backPressedTime: Long = 0

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

        initializeLanguage(this)

        SharedPrefsUtil.init()
        if (SharedPrefsUtil.isInitialized) {
            prefsSnapshot = SharedPrefsUtil.getAllOptions()
        }

        setContent {
            ZuiTweakTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(activity = this)
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        } else {
            Toast.makeText(this, getString(R.string.back_press_exit), Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}

private fun reloadSettings(context: Context) {
    val packagesToReload = mutableListOf<String>()
    if (!SharedPrefsUtil.isInitialized || !::prefsSnapshot.isInitialized) {
        Toast.makeText(context, getString(context, R.string.module_not_loaded), Toast.LENGTH_SHORT).show()
        return
    }

    for (hook in HookManager.getAllHooks()) {
        val oldState = prefsSnapshot.getOrDefault(hook.javaClass.simpleName, hook.menuItem.defaultSelected)
        val newState = SharedPrefsUtil.getOptionValue(hook.javaClass.simpleName, hook.menuItem.defaultSelected)
        if (oldState != newState) {
            packagesToReload.addAll(hook.hookTargetPackage)
        }
    }

    if (packagesToReload.contains("android")) {
        Toast.makeText(context, getString(context, R.string.reboot_required_android), Toast.LENGTH_SHORT).show()
        return
    }
    prefsSnapshot = SharedPrefsUtil.getAllOptions()

    if (packagesToReload.isEmpty()) {
        Toast.makeText(context, getString(context, R.string.no_changes_detected), Toast.LENGTH_SHORT).show()
        return
    }

    val commands = mutableListOf<String>()

    packagesToReload.distinct()
        .filter { it != "android" }
        .forEach { pkg ->
            val result = Shell.cmd("pidof $pkg").exec()
            if (result.isSuccess && result.out.isNotEmpty()) {
                commands.add("killall $pkg")
            }
        }

    if (commands.isEmpty()) {
        Toast.makeText(context, getString(context, R.string.no_running_processes), Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val result = Shell.cmd(*commands.toTypedArray()).exec()
        if (!result.isSuccess) {
            Toast.makeText(context, getString(context, R.string.failed_reload_settings), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, getString(context, R.string.settings_reloaded), Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "${getString(context, R.string.failed_reload_settings)}: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

// ---------------------------------------------------------------------------
// Theme helpers
// ---------------------------------------------------------------------------

@Composable
private fun okColor(): Color =
    if (isSystemInDarkTheme()) Color(0xFF7BD88F) else Color(0xFF2E7D32)

private val cardShape = RoundedCornerShape(20.dp)

// ---------------------------------------------------------------------------
// Root
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(modifier: Modifier = Modifier, activity: Activity? = null) {
    val context = LocalContext.current
    val deviceModel = remember { Constants.deviceModel }
    val isModuleEnabled = remember { SharedPrefsUtil.isInitialized }
    var isRooted by remember { mutableStateOf(false) }
    var region by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var languageDialog by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

    val categories = remember { HookOptions.getHookOptions(context) }
    val filtered = remember(query, categories) {
        if (query.isBlank()) categories
        else categories.mapNotNull { (category, options) ->
            val matches = options.filter {
                it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
            }
            if (matches.isEmpty()) null else category to matches
        }
    }

    val isSupported = deviceModel == "TB371FC" || deviceModel == "TB320FC"

    LaunchedEffect(Unit) {
        isRooted = Util.isDeviceRooted()
        region = Constants.deviceRegion
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = stringResource(R.string.by_stonecold),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settings_view)) },
                            onClick = {
                                menuExpanded = false
                                context.startActivity(Intent(context, SettingsViewActivity::class.java))
                            }
                        )
                        if (isRooted || BuildConfig.DEBUG) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.reload)) },
                                enabled = isRooted && isModuleEnabled,
                                onClick = {
                                    menuExpanded = false
                                    reloadSettings(context)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.language)) },
                            onClick = {
                                menuExpanded = false
                                languageDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.exit)) },
                            onClick = {
                                menuExpanded = false
                                activity?.finish()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 4.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { StatusCard(deviceModel, region, isRooted, isModuleEnabled, isSupported) }

            item {
                SearchField(query = query, onQueryChange = { query = it })
            }

            if (filtered.isEmpty()) {
                item { EmptyState() }
            } else {
                filtered.forEach { (category, options) ->
                    item(key = "h_$category") { SectionHeader(category) }
                    item(key = "c_$category") { OptionGroup(context, options) }
                }
            }

            item { SectionHeader(stringResource(R.string.settings)) }
            item { SettingsCard(context) }

            item { SectionHeader(stringResource(R.string.notice)) }
            item { NoticeCard() }

            item { Footer() }
        }
    }

    if (languageDialog) {
        LanguageDialog(
            current = LanguageUtil.resolveLanguage(),
            onDismiss = { languageDialog = false },
            onSelect = { code ->
                languageDialog = false
                updateLanguage(activity ?: context, code)
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Header / status
// ---------------------------------------------------------------------------

@Composable
fun StatusCard(
    deviceModel: String,
    region: String,
    isRooted: Boolean,
    isModuleEnabled: Boolean,
    isSupported: Boolean
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = deviceModel,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.footer_fork, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(
                    text = stringResource(if (isSupported) R.string.supported else R.string.unsupported_device),
                    good = isSupported
                )
                Pill(
                    text = if (region == "UNKNOWN" || region.isBlank()) stringResource(R.string.unknown) else region,
                    good = region != "UNKNOWN" && region.isNotBlank()
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f))

            StatusRow(
                label = stringResource(R.string.label_root),
                value = stringResource(if (isRooted) R.string.rooted else R.string.not_rooted),
                ok = isRooted
            )
            StatusRow(
                label = stringResource(R.string.label_module),
                value = stringResource(if (isModuleEnabled) R.string.enabled else R.string.disabled),
                ok = isModuleEnabled
            )
        }
    }
}

@Composable
private fun Pill(text: String, good: Boolean) {
    val bg = if (good) {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.errorContainer
    }
    val fg = if (good) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(color = bg, contentColor = fg, shape = RoundedCornerShape(50)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun StatusRow(label: String, value: String, ok: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (ok) okColor() else MaterialTheme.colorScheme.error)
        )
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// ---------------------------------------------------------------------------
// Sections
// ---------------------------------------------------------------------------

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp)
    )
}

@Composable
fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.search_hint)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
fun OptionGroup(context: Context, options: List<HookOption>) {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        options.forEachIndexed { index, option ->
            OptionRow(context = context, option = option)
            if (index != options.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun OptionRow(context: Context, option: HookOption) {
    var checked by remember {
        mutableStateOf(
            SharedPrefsUtil.isInitialized &&
                SharedPrefsUtil.getOptionValue(option.key, option.defaultEnabled)
        )
    }
    val enabled = SharedPrefsUtil.isInitialized && option.isEnabledOption

    LaunchedEffect(option.key, option.isEnabledOption) {
        if (SharedPrefsUtil.isInitialized && !option.isEnabledOption) {
            SharedPrefsUtil.deleteOptionValue(option.key)
        }
    }

    ListItem(
        modifier = Modifier.alpha(if (enabled || !SharedPrefsUtil.isInitialized) 1f else 0.55f),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = {
            Text(
                text = option.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (option.badges.isNotEmpty() || option.targets.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        option.badges.forEach { Badge(it) }
                        if (option.targets.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.target_prefix, option.targets),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                }
                option.reason?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        trailingContent = {
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = {
                    checked = it
                    if (SharedPrefsUtil.isInitialized) {
                        SharedPrefsUtil.setOptionValue(option.key, it)
                    } else {
                        Toast.makeText(context, getString(context, R.string.settings_changed), Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    )
}

@Composable
private fun Badge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun SettingsCard(context: Context) {
    var isIconHidden by remember { mutableStateOf(isLauncherIconHidden(context)) }

    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            headlineContent = {
                Text(
                    text = stringResource(R.string.hide_launcher_icon),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            },
            supportingContent = {
                Text(
                    text = stringResource(R.string.hide_icon_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Switch(
                    checked = isIconHidden,
                    onCheckedChange = {
                        isIconHidden = it
                        setLauncherIconHidden(context, it)
                        Toast.makeText(context, getString(context, R.string.settings_changed), Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

@Composable
fun NoticeCard() {
    Card(
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.feature_not_guaranteed),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(R.string.imported_settings),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.no_results),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun Footer() {
    Text(
        text = stringResource(R.string.footer_fork, BuildConfig.VERSION_NAME),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    )
}

// ---------------------------------------------------------------------------
// Language
// ---------------------------------------------------------------------------

private data class LanguageChoice(val code: String, val labelRes: Int)

@Composable
fun LanguageDialog(current: String, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    val choices = listOf(
        LanguageChoice("en", R.string.english),
        LanguageChoice("ko", R.string.korean),
        LanguageChoice("ru", R.string.russian)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.language)) },
        text = {
            Column {
                choices.forEach { choice ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(choice.code) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = current == choice.code,
                            onClick = { onSelect(choice.code) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(choice.labelRes))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// ---------------------------------------------------------------------------
// Locale plumbing
// ---------------------------------------------------------------------------

fun initializeLanguage(context: Context) {
    Locale.setDefault(Locale(LanguageUtil.resolveLanguage()))
}

fun updateLanguage(context: Context, language: String) {
    LanguageUtil.setLanguage(language)
    Locale.setDefault(Locale(language))
    (context as? Activity)?.recreate()
}

fun isLauncherIconHidden(context: Context): Boolean {
    val componentName = ComponentName(context, MainActivity::class.java)
    val pm = context.packageManager
    val componentEnabledSetting = pm.getComponentEnabledSetting(componentName)
    return componentEnabledSetting == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
}

fun setLauncherIconHidden(context: Context, hidden: Boolean) {
    val componentName = ComponentName(context, MainActivity::class.java)
    val pm = context.packageManager
    pm.setComponentEnabledSetting(
        componentName,
        if (hidden) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
        PackageManager.DONT_KILL_APP
    )
}
