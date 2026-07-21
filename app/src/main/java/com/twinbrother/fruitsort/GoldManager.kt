package com.twinbrother.fruitsort

import android.content.Context
import android.util.Base64
import java.util.Calendar

/**
 * GoldManager – Singleton quản lý vàng và inventory power-ups.
 * Lưu dữ liệu trong SharedPreferences với mã hóa nhẹ Base64+salt.
 */
object GoldManager {

    private const val PREFS_NAME = "economy_data"
    private const val KEY_GOLD = "g"
    private const val KEY_GEMS = "gems"
    private const val KEY_VIP = "vip"
    private const val SALT = "fruitsort_2026"

    // Power-up inventory keys
    private const val KEY_PU_REROLL  = "pu_reroll"
    private const val KEY_PU_REVEAL  = "pu_reveal"
    private const val KEY_PU_SHUFFLE = "pu_shuffle"
    private const val KEY_PU_UNDO    = "pu_undo"
    private const val KEY_PU_HINT    = "pu_hint"

    // Daily & Ads
    private const val KEY_LAST_DAILY_CLAIM = "last_daily_claim"
    private const val KEY_ADS_WATCHED_TODAY = "ads_watched_today"
    private const val KEY_LAST_ADS_DATE = "last_ads_date"

    // Giá power-ups (Vàng)
    const val PRICE_REROLL = 100
    const val PRICE_REVEAL = 150
    const val PRICE_SHUFFLE = 200
    const val PRICE_UNDO    = 150
    const val PRICE_HINT    = 200

    // Vàng thưởng
    const val REWARD_BASE = 50
    const val REWARD_X3 = 150
    const val REWARD_DAILY_AD = 100
    const val MAX_DAILY_ADS = 10

    private fun encode(value: Int): String {
        val raw = "$SALT:$value"
        return Base64.encodeToString(raw.toByteArray(), Base64.NO_WRAP)
    }

    private fun decode(encoded: String): Int {
        return try {
            val raw = String(Base64.decode(encoded, Base64.NO_WRAP))
            if (raw.startsWith("$SALT:")) raw.removePrefix("$SALT:").toInt() else 0
        } catch (e: Exception) {
            0
        }
    }

    // ───── Vàng ─────

    fun getGold(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encoded = prefs.getString(KEY_GOLD, null) ?: return 0
        return decode(encoded)
    }

    fun getGems(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encoded = prefs.getString(KEY_GEMS, null) ?: return 0
        return decode(encoded)
    }

    fun addGold(context: Context, amount: Int) {
        val current = getGold(context)
        val newVal = (current + amount).coerceAtLeast(0)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_GOLD, encode(newVal)).apply()
    }

    fun addGems(context: Context, amount: Int) {
        val current = getGems(context)
        val newVal = (current + amount).coerceAtLeast(0)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_GEMS, encode(newVal)).apply()
    }

    /**
     * @return true nếu đủ vàng và trừ thành công, false nếu không đủ
     */
    fun spendGold(context: Context, amount: Int): Boolean {
        val current = getGold(context)
        if (current < amount) return false
        val newVal = current - amount
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_GOLD, encode(newVal)).apply()
        return true
    }

    // ───── Daily Login & Video Ads ─────

    fun canClaimDaily(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastClaim = prefs.getLong(KEY_LAST_DAILY_CLAIM, 0)
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return lastClaim < today
    }

    fun claimDaily(context: Context): Int {
        if (!canClaimDaily(context)) return 0
        val rewardGold = 200
        val rewardGems = 3
        addGold(context, rewardGold)
        addGems(context, rewardGems)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putLong(KEY_LAST_DAILY_CLAIM, System.currentTimeMillis()).apply()
        return rewardGold
    }

    fun getAdsWatchedToday(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastDate = prefs.getLong(KEY_LAST_ADS_DATE, 0)
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        if (lastDate < today) {
            prefs.edit().putInt(KEY_ADS_WATCHED_TODAY, 0).apply()
            return 0
        }
        return prefs.getInt(KEY_ADS_WATCHED_TODAY, 0)
    }

    fun watchAdForGold(context: Context): Boolean {
        val current = getAdsWatchedToday(context)
        if (current >= MAX_DAILY_ADS) return false
        
        addGold(context, REWARD_DAILY_AD)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_ADS_WATCHED_TODAY, current + 1)
            .putLong(KEY_LAST_ADS_DATE, System.currentTimeMillis())
            .apply()
        return true
    }

    // ───── VIP ─────

    fun isVip(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_VIP, false)
    }

    fun setVip(context: Context, vip: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_VIP, vip).apply()
    }

    // ───── Power-up Inventory ─────

    fun getRerollCount(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_PU_REROLL, 0)

    fun getRevealCount(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_PU_REVEAL, 0)

    fun getShuffleCount(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_PU_SHUFFLE, 0)

    fun getUndoCount(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_PU_UNDO, 0)

    fun getHintCount(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getInt(KEY_PU_HINT, 0)

    fun addReroll(context: Context, amount: Int = 1) = addPowerup(context, KEY_PU_REROLL, amount)
    fun addReveal(context: Context, amount: Int = 1) = addPowerup(context, KEY_PU_REVEAL, amount)
    fun addShuffle(context: Context, amount: Int = 1) = addPowerup(context, KEY_PU_SHUFFLE, amount)
    fun addUndo(context: Context, amount: Int = 1) = addPowerup(context, KEY_PU_UNDO, amount)
    fun addHint(context: Context, amount: Int = 1) = addPowerup(context, KEY_PU_HINT, amount)

    /** Trừ 1 powerup, return true nếu còn hàng */
    fun useReroll(context: Context): Boolean = usePowerup(context, KEY_PU_REROLL)
    fun useReveal(context: Context): Boolean = usePowerup(context, KEY_PU_REVEAL)
    fun useShuffle(context: Context): Boolean = usePowerup(context, KEY_PU_SHUFFLE)
    fun useUndo(context: Context): Boolean = usePowerup(context, KEY_PU_UNDO)
    fun useHint(context: Context): Boolean = usePowerup(context, KEY_PU_HINT)

    private fun addPowerup(context: Context, key: String, amount: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + amount).apply()
    }

    private fun usePowerup(context: Context, key: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(key, 0)
        if (current <= 0) return false
        prefs.edit().putInt(key, current - 1).apply()
        return true
    }

    /** Mua power-up bằng vàng */
    fun buyReroll(context: Context): Boolean {
        if (!spendGold(context, PRICE_REROLL)) return false
        addReroll(context)
        return true
    }

    fun buyReveal(context: Context): Boolean {
        if (!spendGold(context, PRICE_REVEAL)) return false
        addReveal(context)
        return true
    }

    fun buyShuffle(context: Context): Boolean {
        if (!spendGold(context, PRICE_SHUFFLE)) return false
        addShuffle(context)
        return true
    }

    fun buyUndo(context: Context): Boolean {
        if (!spendGold(context, PRICE_UNDO)) return false
        addUndo(context)
        return true
    }

    fun buyHint(context: Context): Boolean {
        if (!spendGold(context, PRICE_HINT)) return false
        addHint(context)
        return true
    }

    /** Đổi 500 Gold -> 5 Gems */
    fun exchangeGoldForGems(context: Context): Boolean {
        if (!spendGold(context, 500)) return false
        addGems(context, 5)
        return true
    }

    /** Đổi 5 Gems -> 500 Gold */
    fun exchangeGemsForGold(context: Context): Boolean {
        if (!spendGems(context, 5)) return false
        addGold(context, 500)
        return true
    }

    // ───── Level Stars ─────

    fun setLevelStars(context: Context, levelId: Int, stars: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = "stars_$levelId"
        val currentStars = prefs.getInt(key, 0)
        if (stars > currentStars) {
            prefs.edit().putInt(key, stars).apply()
        }
    }

    fun getLevelStars(context: Context, levelId: Int): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt("stars_$levelId", 0)
    }

    // ───── Gems Spending ─────

    fun spendGems(context: Context, amount: Int): Boolean {
        val current = getGems(context)
        if (current < amount) return false
        val newVal = current - amount
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_GEMS, encode(newVal)).apply()
        return true
    }

    // Gem costs
    const val GEM_COST_UNDO = 1
    const val GEM_COST_EXTRA_TURNS = 2
    const val GEM_COST_HINT = 3

    // Gem rewards
    const val GEM_REWARD_3_STARS = 1
    const val GEM_REWARD_BOSS = 3
    const val GEM_REWARD_COMBO_4 = 1
}
