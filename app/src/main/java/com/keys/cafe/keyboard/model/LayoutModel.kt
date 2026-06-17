package com.keys.cafe.keyboard.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a complete keyboard layout configuration.
 *
 * Note: spacing/sizing fields use nullable backing properties because most
 * layout JSON assets omit them entirely. Gson's reflection-based parser does
 * NOT apply Kotlin default parameter values for missing JSON fields, so any
 * field that might be absent from JSON must be nullable here.
 */
@Parcelize
data class LayoutModel(
    val id: String,
    val name: String,
    val locale: String,
    val type: LayoutType,
    val rows: List<RowModel>,
    private val defaultKeyWidth: Float? = null,
    private val defaultKeyHeight: Float? = null,
    private val horizontalGap: Float? = null,
    private val verticalGap: Float? = null,
    private val paddingStart: Float? = null,
    private val paddingEnd: Float? = null,
    private val paddingTop: Float? = null,
    private val paddingBottom: Float? = null
) : Parcelable {

    val defaultKeyWidthOrDefault: Float get() = defaultKeyWidth ?: 1.0f
    val defaultKeyHeightOrDefault: Float get() = defaultKeyHeight ?: 1.0f
    val horizontalGapOrDefault: Float get() = horizontalGap ?: 4f
    val verticalGapOrDefault: Float get() = verticalGap ?: 5f
    val paddingStartOrDefault: Float get() = paddingStart ?: 6f
    val paddingEndOrDefault: Float get() = paddingEnd ?: 6f
    val paddingTopOrDefault: Float get() = paddingTop ?: 10f
    val paddingBottomOrDefault: Float get() = paddingBottom ?: 16f

    enum class LayoutType {
        ALPHABET, NUMBER, SYMBOL, EXTENDED_SYMBOL, EMOJI
    }
}

@Parcelize
data class RowModel(
    val keys: List<KeyModel>,
    private val indent: Float? = null,
    private val height: Float? = null
) : Parcelable {
    val indentOrDefault: Float get() = indent ?: 0f
    val heightOrDefault: Float get() = height ?: 1.0f
}
