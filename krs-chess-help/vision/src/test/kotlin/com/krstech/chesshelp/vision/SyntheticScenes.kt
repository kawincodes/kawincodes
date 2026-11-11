package com.krstech.chesshelp.vision

import java.awt.Color

/** The synthetic screenshots shared by the locator, sampler and fingerprint tests. */
object SyntheticScenes {

    private val LIGHT_PAGE = Color(237, 235, 233)
    private val DARK_PAGE = Color(22, 21, 18)

    /** Plain boards: every theme, several screen sizes, offsets and (fractional) cell sizes. */
    val plain: List<Scene> = listOf(
        Scene(1080, 2400, 0, 420, 1080, Themes.GREEN, name = "full width, start position"),
        Scene(1080, 2400, 39, 540, 1002, Themes.BROWN, Positions.MIDDLE, pageColor = LIGHT_PAGE, name = "fractional cells"),
        Scene(1080, 2400, 24, 310, 1032, Themes.BLUE, Positions.PAWN_ENDING, frameWidth = 12, pageColor = DARK_PAGE, name = "framed board"),
        Scene(1080, 2400, 140, 700, 800, Themes.GREY, Positions.MIDDLE_FLIPPED, pageColor = LIGHT_PAGE, name = "black at bottom"),
        Scene(1080, 2400, 351, 260, 378, Themes.PURPLE, name = "35 percent of the width"),
        Scene(1080, 2400, 60, 1150, 960, Themes.PALE, Positions.MIDDLE, pageColor = Color(255, 255, 255), name = "low contrast theme"),
        Scene(1080, 2400, 7, 211, 1066, Themes.NIGHT, Positions.MIDDLE, pageColor = DARK_PAGE, name = "dark theme, odd offset"),
        Scene(720, 1560, 0, 330, 720, Themes.GREEN, name = "720 wide"),
        Scene(720, 1560, 30, 212, 656, Themes.BROWN, Positions.MIDDLE, pageColor = LIGHT_PAGE, name = "720 wide inset"),
        Scene(1440, 3120, 45, 800, 1344, Themes.BROWN, Positions.MIDDLE, name = "1440 wide"),
        Scene(1440, 3120, 0, 1100, 1440, Themes.BLUE, Positions.SPARSE, pageColor = DARK_PAGE, name = "1440 wide full width"),
        Scene(2000, 1200, 152, 126, 888, Themes.GREEN, Positions.MIDDLE, pageColor = LIGHT_PAGE, name = "landscape tablet"),
        Scene(2000, 1200, 420, 20, 1160, Themes.GREY, name = "landscape, nearly full height"),
        Scene(1200, 2000, 100, 500, 1000, Themes.PURPLE, Positions.PAWN_ENDING, pageColor = LIGHT_PAGE, name = "portrait tablet"),
    )

    /** Boards carrying everything apps draw on top of the squares. */
    val decorated: List<Scene> = listOf(
        Scene(
            1080, 2400, 39, 540, 1002, Themes.GREEN, Positions.MIDDLE, lastMove = intArrayOf(51, 35), selected = 45,
            dots = intArrayOf(28, 30, 37, 39), rings = intArrayOf(27), name = "last move, selection, dots, ring",
        ),
        Scene(
            1080, 2400, 0, 300, 1080, Themes.BROWN, Positions.MIDDLE, lastMove = intArrayOf(11, 27), check = 62,
            dots = intArrayOf(16, 17, 24, 25, 33), cornerHints = intArrayOf(26), pageColor = LIGHT_PAGE,
            name = "check, corner hints, dots",
        ),
        Scene(
            1080, 2400, 20, 640, 1040, Themes.BLUE, Positions.MIDDLE, arrow = intArrayOf(52, 20), lastMove = intArrayOf(10, 26),
            pageColor = DARK_PAGE, name = "arrow across the board",
        ),
        Scene(
            2000, 1200, 152, 126, 888, Themes.GREEN, Positions.MIDDLE, arrow = intArrayOf(58, 30), lastMove = intArrayOf(51, 35),
            dots = intArrayOf(19, 28), pageColor = LIGHT_PAGE, name = "landscape with arrow",
        ),
        Scene(
            720, 1560, 0, 330, 720, Themes.GREY, Positions.SPARSE, lastMove = intArrayOf(44, 52), check = 13,
            dots = intArrayOf(43, 45, 51, 53, 59, 60, 61), name = "small sparse board with king moves",
        ),
    )

    /** Boards with little or nothing on them and boards with (mild) texture. */
    val special: List<Scene> = listOf(
        Scene(1080, 2400, 0, 420, 1080, Themes.BROWN, Positions.EMPTY, name = "empty board"),
        Scene(1080, 2400, 90, 900, 900, Themes.GREEN, Positions.EMPTY, pageColor = LIGHT_PAGE, coords = false, name = "empty board without labels"),
        Scene(1080, 2400, 0, 420, 1080, Themes.BLUE, Positions.SPARSE, pageColor = DARK_PAGE, name = "three pieces"),
        Scene(1080, 2340, 0, 480, 1080, Themes.BROWN, Positions.MIDDLE, texture = 7, name = "textured squares"),
        Scene(1080, 2400, 30, 500, 1020, Themes.GREEN, Positions.START, texture = 5, pageColor = LIGHT_PAGE, name = "textured start position"),
        Scene(1080, 2400, 0, 0, 1080, Themes.GREEN, Positions.MIDDLE, clutter = false, name = "board in the top-left corner"),
        Scene(1080, 1080, 0, 0, 1080, Themes.BROWN, Positions.START, clutter = false, name = "frame is exactly the board"),
    )

    val all: List<Scene> get() = plain + decorated + special
}
