package com.ohshootstudio.resibooth.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Activation : Screen("activation")
    object Standby : Screen("standby")
    object LayoutSelect : Screen("layout_select")
    object Capture : Screen("capture")
    object Preview : Screen("preview")
    object Output : Screen("output")
    object Done : Screen("done")
}

