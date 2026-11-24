package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val localeId = Locale("id", "ID")

fun formatRupiah(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(localeId)
    return "Rp ${formatter.format(amount)}"
}

fun formatCompactRupiah(amount: Long): String {
    return when {
        amount >= 1_000_000_000L -> String.format(localeId, "%.1f M", amount / 1_000_000_000.0)
        amount >= 1_000_000L -> {
            val jt = amount / 1_000_000.0
            if (jt % 1.0 == 0.0) "${jt.toInt()} jt" else String.format(localeId, "%.1f jt", jt)
        }
        amount >= 1_000L -> "${amount / 1_000L} rb"
        else -> "$amount"
    }
}

fun startOfDayMillis(timeMillis: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = timeMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return cal.timeInMillis
}

fun endOfDayMillis(timeMillis: Long): Long {
    val cal = Calendar.getInstance().apply {
        timeInMillis = timeMillis
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return cal.timeInMillis
}

fun formatDateGroupLabel(dateMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
    val targetDay = startOfDayMillis(dateMillis)
    val today = startOfDayMillis(nowMillis)
    val yesterday = today - 86_400_000L
    return when (targetDay) {
        today -> "Hari Ini"
        yesterday -> "Kemarin"
        else -> SimpleDateFormat("EEEE, dd MMM yyyy", localeId).format(Date(dateMillis))
            .replaceFirstChar { it.uppercase(localeId) }
    }
}

fun formatShortDate(dateMillis: Long): String {
    return SimpleDateFormat("dd MMM yyyy", localeId).format(Date(dateMillis))
}

fun formatDayShort(dateMillis: Long): String {
    return when (Calendar.getInstance().apply { timeInMillis = dateMillis }.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Sen"
        Calendar.TUESDAY -> "Sel"
        Calendar.WEDNESDAY -> "Rab"
        Calendar.THURSDAY -> "Kam"
        Calendar.FRIDAY -> "Jum"
        Calendar.SATURDAY -> "Sab"
        Calendar.SUNDAY -> "Min"
        else -> ""
    }
}

fun formatDayMonth(dateMillis: Long): String {
    return SimpleDateFormat("dd/MM", localeId).format(Date(dateMillis))
}

fun formatMonthShort(dateMillis: Long): String {
    return SimpleDateFormat("MMM", localeId).format(Date(dateMillis))
}

fun formatMonthYear(dateMillis: Long): String {
    return SimpleDateFormat("MMMM yyyy", localeId).format(Date(dateMillis))
        .replaceFirstChar { it.uppercase(localeId) }
}

data class IconOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

object KasIconCatalog {
    val categoryIcons = listOf(
        IconOption("storefront", "Usaha", Icons.Rounded.Storefront),
        IconOption("work", "Kerja", Icons.Rounded.Work),
        IconOption("laptop", "Digital", Icons.Rounded.LaptopMac),
        IconOption("card_giftcard", "Bonus", Icons.Rounded.CardGiftcard),
        IconOption("trending_up", "Investasi", Icons.Rounded.AutoGraph),
        IconOption("payments", "Tunai", Icons.Rounded.Payments),
        IconOption("local_cafe", "Kuliner", Icons.Rounded.LocalCafe),
        IconOption("shopping_bag", "Dagang", Icons.Rounded.ShoppingBag),
        IconOption("directions_car", "Jasa", Icons.Rounded.DirectionsCar),
        IconOption("home", "Properti", Icons.Rounded.Home),
        IconOption("savings", "Tabungan", Icons.Rounded.Savings),
        IconOption("star", "Lainnya", Icons.Rounded.Star)
    )

    val pocketIcons = listOf(
        IconOption("wallet", "Dompet", Icons.Rounded.AccountBalanceWallet),
        IconOption("account_balance", "Bank", Icons.Rounded.AccountBalance),
        IconOption("savings", "Tabungan", Icons.Rounded.Savings),
        IconOption("payments", "Kas", Icons.Rounded.Payments),
        IconOption("storefront", "Modal", Icons.Rounded.Storefront),
        IconOption("star", "Target", Icons.Rounded.Star)
    )

    fun resolveIcon(key: String): ImageVector {
        return categoryIcons.firstOrNull { it.key == key }?.icon
            ?: pocketIcons.firstOrNull { it.key == key }?.icon
            ?: Icons.Rounded.Payments
    }
}

object KasColorCatalog {
    val swatches = listOf(
        0xFF0D6E4F, // Emerald
        0xFF0284C7, // Ocean Blue
        0xFF7C3AED, // Royal Violet
        0xFFD97706, // Warm Amber
        0xFFE11D48, // Coral Rose
        0xFF0891B2, // Teal Cyan
        0xFF4F46E5, // Deep Indigo
        0xFFEA580C, // Sunset Orange
        0xFF16A34A, // Leaf Green
        0xFFDB2777, // Berry Pink
        0xFF0F766E, // Pine Teal
        0xFF64748B  // Cool Slate
    )

    fun toColor(hex: Long): Color = Color(hex)

    fun toHexString(colorHex: Long): String {
        val rgb = (colorHex and 0xFFFFFFL)
        return String.format("#%06X", rgb)
    }

    fun hsvToColorLong(hue: Float, saturation: Float, value: Float): Long {
        val argb = android.graphics.Color.HSVToColor(
            floatArrayOf(
                hue.coerceIn(0f, 360f),
                saturation.coerceIn(0f, 1f),
                value.coerceIn(0f, 1f)
            )
        )
        return argb.toLong() and 0xFFFFFFFFL
    }

    fun colorLongToHsv(colorHex: Long): FloatArray {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(colorHex.toInt(), hsv)
        return hsv
    }
}
