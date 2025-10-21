package com.example.cinefreak

import android.content.Context
import com.lagradost.cloudstream3.plugins.Plugin
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin

@CloudstreamPlugin
class CinefreakPlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(CinefreakProvider())
    }
}
