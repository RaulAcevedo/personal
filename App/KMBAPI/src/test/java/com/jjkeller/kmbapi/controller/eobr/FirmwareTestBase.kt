package com.jjkeller.kmbapi.controller.EOBR

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import com.google.gson.Gson
import com.jjkeller.kmbapi.R
import com.jjkeller.kmbapi.common.TestBase
import com.jjkeller.kmbapi.configuration.AppSettings
import com.jjkeller.kmbapi.configuration.FirmwareUpdate
import com.jjkeller.kmbapi.configuration.GlobalState
import com.jjkeller.kmbapi.controller.interfaces.IEobrService
import com.jjkeller.kmbapi.kmbeobr.Enums
import org.mockito.Matchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService

open class FirmwareTestBase : TestBase() {
    protected val eobrService = mock(IEobrService::class.java)
    protected val eobrReader = mock(IEobrReader::class.java)
    protected val context = mock(Context::class.java)
    private val featureService = mock(IFeatureToggleService::class.java)

    init {
        `when`(eobrService.context).thenReturn(context)
        mockContext()
        GlobalState.getInstance().setfeatureToggleService(featureService)
        setupBundledFirmwareVersions(context)
    }

    private fun mockContext() {
        `when`(context.getString(R.string.rc)).thenReturn(R.string.rc.toString())
        `when`(context.getString(R.string.mainfirmwarerevision))
                .thenReturn(R.string.mainfirmwarerevision.toString())

        val resources = mock(Resources::class.java)
        val resourceAsStream = this.javaClass.classLoader.getResourceAsStream("res/raw/firmware_updates.json")
        `when`(resources.openRawResource(Matchers.anyInt()))
                .thenReturn(resourceAsStream)
        `when`(context.resources).thenReturn(resources)
    }
    private fun setupBundledFirmwareVersions(context: Context) {
        val appSettings = Mockito.mock(AppSettings::class.java)
        var fwUpdates = arrayOf(FirmwareUpdate())

        val gson = Gson()
        val settings = context.resources.openRawResource(R.raw.firmware_updates)
        val s = Scanner(settings).useDelimiter("\\A")
        val json = if (s.hasNext()) s.next() else null

        if (json != null) {
            fwUpdates = gson.fromJson(json, Array<FirmwareUpdate>::class.java)
        }

        `when`(appSettings.firmwareUpdates).thenReturn(fwUpdates)
        GlobalState.getInstance().setAppSettings(appSettings)
    }

    protected fun mockInstalledFirmware(installedVersion: String) {
        val bundle = mock(Bundle::class.java)

        `when`(bundle.getInt(R.string.rc.toString())).thenReturn(Enums.EobrReturnCode.S_SUCCESS)
        `when`(bundle.getString(R.string.mainfirmwarerevision.toString()))
                .thenReturn(installedVersion)
        `when`(eobrReader.Technician_GetEOBRRevisions()).thenReturn(bundle)
    }

    protected fun enableMandate() {
        `when`(featureService.isEldMandateEnabled).thenReturn(true)
    }

}