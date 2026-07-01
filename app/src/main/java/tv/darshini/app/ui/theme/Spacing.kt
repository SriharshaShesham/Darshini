package tv.darshini.app.ui.theme

import tv.darshini.app.ui.design.AppSpacing
import tv.darshini.app.ui.design.LocalAppSpacing

typealias Spacing = AppSpacing

val LocalSpacing = LocalAppSpacing

fun defaultSpacing(): Spacing = AppSpacing()
