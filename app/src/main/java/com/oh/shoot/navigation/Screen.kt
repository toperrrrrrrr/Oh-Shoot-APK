package com.oh.shoot.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Standby : Screen("standby")
    object LayoutSelect : Screen("layout_select")
    object Capture : Screen("capture")
    object Preview : Screen("preview")
    object Output : Screen("output")
    object Done : Screen("done")
}
