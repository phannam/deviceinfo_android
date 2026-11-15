package vn.npstudio.deviceinfo.feature.screentest

/** Full-screen patterns, shown in declaration order. */
enum class ScreenTestPattern {
    /** Stuck or bright pixels, backlight bleed. */
    BLACK,

    /** Dead or dark pixels. */
    WHITE,

    /** Stuck sub-pixels. */
    RED,
    GREEN,
    BLUE,

    /** Burn-in, mura, discoloration. */
    GRAY,

    /** Banding and horizontal lines. */
    GRADIENT_HORIZONTAL,

    /** Vertical lines. */
    GRADIENT_VERTICAL,

    /** 1 px checkerboard for line defects and flicker. */
    CHECKERBOARD,
}
