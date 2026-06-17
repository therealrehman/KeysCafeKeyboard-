package com.keys.cafe.keyboard.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a complete keyboard layout configuration.
 */
@Parcelize
data class LayoutModel(
    val id: String,
    val name: String,
    val locale: String,
    val type: LayoutType,
    val rows: List<RowModel>,
    val defaultKeyWidth: Float = 1.0f,
    val defaultKeyHeight: Float = 1.0f,
    val horizontalGap: Float = 4f,
    val verticalGap: Float = 5f,
    val paddingStart: Float = 6f,
    val paddingEnd: Float = 6f,
    val paddingTop: Float = 10f,
    val paddingBottom: Float = 16f
) : Parcelable {

    enum class LayoutType {
        ALPHABET, NUMBER, SYMBOL, EXTENDED_SYMBOL, EMOJI
    }
}

@Parcelize
data class RowModel(
    val keys: List<KeyModel>,
    val indent: Float = 0f,
    val height: Float = 1.0f
) : Parcelable
