package kr.stonecold.zuitweak

import android.content.Context
import kr.stonecold.zuitweak.common.*
import kr.stonecold.zuitweak.hooks.HookMenuCategory

object HookOptions {
    fun getHookOptions(context: Context): List<Pair<String, List<HookOption>>> {
        val categoryOrder = listOf(
            HookMenuCategory.COMMON to LanguageUtil.getString(R.string.hook_menu_category_common),
            HookMenuCategory.ROW to LanguageUtil.getString(R.string.hook_menu_category_row),
            HookMenuCategory.PRC to LanguageUtil.getString(R.string.hook_menu_category_prc),
            HookMenuCategory.DEVICE to LanguageUtil.getString(R.string.hook_menu_category_device),
            HookMenuCategory.UNFUCKZUI to LanguageUtil.getString(R.string.hook_menu_category_unfuckzui),
            HookMenuCategory.DEVELOPMENT to LanguageUtil.getString(R.string.hook_menu_category_development),
        )

        val categoriesWithOptions = mutableMapOf<String, MutableList<HookOption>>()
        val otherCategory = context.getString(R.string.hook_menu_category_other)

        val hooks = HookManager.getAllHooks()

        hooks.filter { hook -> !hook.menuItem.isDebug || BuildConfig.DEBUG }
            .forEach { hook ->
                val categoryName = categoryOrder.find { it.first == hook.menuItem.category }?.second ?: otherCategory

                val (isEnabled, message) = hook.isEnabledMenu(context)

                // Short constraint badges, e.g. ROW / TB320FC
                val badges = buildList {
                    addAll(hook.hookTargetRegion)
                    addAll(hook.hookTargetDevice)
                }

                // Turn the debug string ("PackageMatch: false, …") into a human reason
                val reason = when {
                    message.contains("PackageMatch") -> context.getString(R.string.reason_package)
                    message.contains("DeviceMatch") -> context.getString(R.string.reason_device)
                    message.contains("RegionMatch") -> context.getString(R.string.reason_region)
                    message.contains("VersionMatch") -> context.getString(R.string.reason_version)
                    message.contains("CustomEnabled") -> context.getString(R.string.reason_custom)
                    else -> null
                }

                val option = HookOption(
                    key = hook.javaClass.simpleName,
                    title = hook.menuItem.title,
                    description = hook.menuItem.description,
                    targets = (hook.hookTargetPackage + hook.hookTargetPackageOptional).joinToString(),
                    badges = badges,
                    reason = if (isEnabled) null else reason,
                    isEnabledOption = isEnabled,
                    defaultEnabled = hook.menuItem.defaultSelected,
                )

                categoriesWithOptions.computeIfAbsent(categoryName) { mutableListOf() }.add(option)
            }

        return categoryOrder.mapNotNull { (_, name) ->
            categoriesWithOptions[name]?.let { name to it }
        }
    }
}

data class HookOption(
    val key: String,
    val title: String,
    val description: String,
    val targets: String,
    val badges: List<String>,
    val reason: String?,
    val isEnabledOption: Boolean,
    val defaultEnabled: Boolean,
)
