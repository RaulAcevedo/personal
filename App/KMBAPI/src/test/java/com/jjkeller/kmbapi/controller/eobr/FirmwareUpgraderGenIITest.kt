package com.jjkeller.kmbapi.controller.EOBR

import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import com.jjkeller.kmbapi.configuration.FirmwareUpdate
import com.jjkeller.kmbapi.configuration.GlobalState
import com.jjkeller.kmbapi.controller.dataaccess.EobrConfigurationFacade
import com.jjkeller.kmbapi.controller.interfaces.IFacadeFactory
import com.jjkeller.kmbapi.controller.utility.IRESTWebServiceHelper
import com.jjkeller.kmbapi.controller.utility.RestWebServiceHelperFactory
import com.jjkeller.kmbapi.eobrengine.EobrEngineBase
import com.jjkeller.kmbapi.eobrengine.IEobrEngine
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService
import com.jjkeller.kmbapi.kmbeobr.Constants
import com.jjkeller.kmbapi.proxydata.FirmwareVersion
import com.spun.util.Asserts.assertEqual
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Matchers.*
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class FirmwareUpgraderGenIITest : FirmwareTestBase() {

    private val fwImageDownloader = mock(IFirmwareImageDownloader::class.java)
    private val facadeFactory = mock(IFacadeFactory::class.java)
    private val featureService = mock(IFeatureToggleService::class.java)
    private val networkInfo = mock(NetworkInfo::class.java)

    @Before
    fun setup() {
        `when`(facadeFactory.getEobrConfigurationFacade(any()))
                .thenReturn(mock(EobrConfigurationFacade::class.java))

        val eobrEngine = mock(IEobrEngine::class.java)
        val hardwareBundle = mockHardwareBundle()
        `when`(eobrEngine.GetEobrHardware()).thenReturn(hardwareBundle)
        `when`(eobrReader.eobrEngine).thenReturn(eobrEngine)

        `when`(featureService.ignoreFirmwareUpdate).thenReturn(false)
        `when`(networkInfo.isAvailable).thenReturn(true)
        val network = mock(ConnectivityManager::class.java)
        `when`(network.activeNetworkInfo).thenReturn(networkInfo)
        `when`(context.getSystemService(anyString())).thenReturn(network)
    }

    @Test
    fun test_IgnoreFirmwareUpdates_ReturnsNoUpdateNeeded() {
        val installedFirmwareVersion = "6.88.0"
        val networkFirmwareVersion = "6.88.13"
        GlobalState.getInstance().setfeatureToggleService(featureService)
        `when`(featureService.ignoreFirmwareUpdate).thenReturn(true)
        mockInstalledFirmware(installedFirmwareVersion)
        mockServerFirmware(networkFirmwareVersion)
        val firmwareUpdate = buildFirmwareUpdate(installedFirmwareVersion)
        val sut = FirmwareUpgraderGenII(eobrReader, eobrService, firmwareUpdate, fwImageDownloader, facadeFactory)

        val isUpdateRequired = sut.isFirmwareUpgradeRequired

        assertFalse(isUpdateRequired)
    }

    @Test
    fun test6_88_0_MandateOn_NoNetwork_ReturnsUpdateNeededToMinMandateVersion() {
        val installedFirmwareVersion = "6.88.0"
        enableMandate()
        mockInstalledFirmware(installedFirmwareVersion)
        mockNoNetworkConnection()
        val firmwareUpdate = buildFirmwareUpdate(installedFirmwareVersion)
        val sut = FirmwareUpgraderGenII(eobrReader, eobrService, firmwareUpdate, fwImageDownloader, facadeFactory)

        val isUpdateRequired = sut.isFirmwareUpgradeRequired

        assertTrue(isUpdateRequired)
        val expectedVersion = FirmwareUpgraderGenII.MIN_MANDATE_VERSION
        assertEqual("Expected version to update to is $expectedVersion",
                expectedVersion, sut.firmwareUpdateConfig.version)
    }

    @Test
    fun test6_88_0_MandateOff_NetworkOff_ReturnsNoUpdateNeeded() {
        val installedFirmwareVersion = "6.88.0"
        mockInstalledFirmware(installedFirmwareVersion)
        mockNoNetworkConnection()
        val firmwareUpdate = buildFirmwareUpdate(installedFirmwareVersion)
        val sut = FirmwareUpgraderGenII(eobrReader, eobrService, firmwareUpdate, fwImageDownloader, facadeFactory)

        val isUpdateRequired = sut.isFirmwareUpgradeRequired

        assertFalse(isUpdateRequired)
    }

    @Test
    fun test6_88_0_MandateOff_NetworkReturns_6_88_0_ReturnsNoUpdateNeeded() {
        val installedFirmwareVersion = "6.88.0"
        val networkFirmwareVersion = "6.88.0"
        mockInstalledFirmware(installedFirmwareVersion)
        mockServerFirmware(networkFirmwareVersion)
        val firmwareUpdate = buildFirmwareUpdate(installedFirmwareVersion)
        val sut = FirmwareUpgraderGenII(eobrReader, eobrService, firmwareUpdate, fwImageDownloader, facadeFactory)

        val isUpdateRequired = sut.isFirmwareUpgradeRequired

        assertFalse(isUpdateRequired)
    }

    @Test
    fun test6_88_0_MandateOn_NetworkReturnsAboveMandateMinimum_ReturnsUpdateNeededToNetworkVersion() {
        val installedFirmwareVersion = "6.88.0"
        val networkFirmwareVersion = "6.88.114"
        mockInstalledFirmware(installedFirmwareVersion)
        mockServerFirmware(networkFirmwareVersion)
        val firmwareUpdate = buildFirmwareUpdate(installedFirmwareVersion)
        val sut = FirmwareUpgraderGenII(eobrReader, eobrService, firmwareUpdate, fwImageDownloader, facadeFactory)

        val isUpdateRequired = sut.isFirmwareUpgradeRequired

        assertTrue(isUpdateRequired)
        assertEqual("Expected to use network version $networkFirmwareVersion instead of mandate minimum ${FirmwareUpgraderGenII.MIN_MANDATE_VERSION}",
                networkFirmwareVersion, sut.firmwareUpdateConfig.version)
    }

    @Test
    fun test6_88_113_MandateOn_NetworkReturnsBelowMandateMinimum_ReturnsNoUpdateNeeded() {
        val installedFirmwareVersion = "6.88.113"
        val networkFirmwareVersion = "6.88.0"
        enableMandate()
        mockInstalledFirmware(installedFirmwareVersion)
        mockServerFirmware(networkFirmwareVersion)
        val firmwareUpdate = buildFirmwareUpdate(installedFirmwareVersion)
        val sut = FirmwareUpgraderGenII(eobrReader, eobrService, firmwareUpdate, fwImageDownloader, facadeFactory)

        val isUpdateRequired = sut.isFirmwareUpgradeRequired

        assertFalse(isUpdateRequired)
    }

    @Test
    fun test6_88_113_MandateOff_NoNetwork_ReturnsNoUpdateNeeded() {
        val installedFirmwareVersion = "6.88.113"
        mockInstalledFirmware(installedFirmwareVersion)
        mockNoNetworkConnection()
        val firmwareUpdate = buildFirmwareUpdate(installedFirmwareVersion)
        val sut = FirmwareUpgraderGenII(eobrReader, eobrService, firmwareUpdate, fwImageDownloader, facadeFactory)

        val isUpdateRequired = sut.isFirmwareUpgradeRequired

        assertFalse(isUpdateRequired)
    }

    @Test
    fun test6_88_113_MandateOff_NetworkReturnsBelowMandateMinimum_ReturnsUpdateNeeded() {
        val installedFirmwareVersion = "6.88.113"
        val networkFirmwareVersion = "6.88.0"
        mockInstalledFirmware(installedFirmwareVersion)
        mockServerFirmware(networkFirmwareVersion)
        val firmwareUpdate = buildFirmwareUpdate(installedFirmwareVersion)
        val sut = FirmwareUpgraderGenII(eobrReader, eobrService, firmwareUpdate, fwImageDownloader, facadeFactory)

        val isUpdateRequired = sut.isFirmwareUpgradeRequired

        assertTrue(isUpdateRequired)
        assertEqual("Expected version to downgrade to is $networkFirmwareVersion",
                networkFirmwareVersion, sut.firmwareUpdateConfig.version)
    }

    @Test
    fun test6_88_113_NetworkReturns_6_88_113_ReturnsNoUpdateNeeded() {
        val installedFirmwareVersion = "6.88.113"
        val networkFirmwareVersion = "6.88.113"
        mockInstalledFirmware(installedFirmwareVersion)
        mockServerFirmware(networkFirmwareVersion)
        val firmwareUpdate = buildFirmwareUpdate(installedFirmwareVersion)
        val sut = FirmwareUpgraderGenII(eobrReader, eobrService, firmwareUpdate, fwImageDownloader, facadeFactory)

        val isUpdateRequired = sut.isFirmwareUpgradeRequired

        assertFalse(isUpdateRequired)
    }

    @Test
    fun test6_88_113_NetworkReturnsHigherVersion_ReturnsUpdateNeeded() {
        val installedFirmwareVersion = "6.88.113"
        val networkFirmwareVersion = "6.88.114"
        mockInstalledFirmware(installedFirmwareVersion)
        mockServerFirmware(networkFirmwareVersion)
        val firmwareUpdate = buildFirmwareUpdate(installedFirmwareVersion)
        val sut = FirmwareUpgraderGenII(eobrReader, eobrService, firmwareUpdate, fwImageDownloader, facadeFactory)

        val isUpdateRequired = sut.isFirmwareUpgradeRequired

        assertTrue(isUpdateRequired)
        assertEqual("Expected version to update to is $networkFirmwareVersion",
                networkFirmwareVersion, sut.firmwareUpdateConfig.version)
    }

    @Test
    fun test6_88_113_MandateOff_NetworkReturnsPatchDowngrade_ReturnsUpdateNeeded() {
        val installedFirmwareVersion = "6.88.113"
        val networkFirmwareVersion = "6.88.112"
        mockInstalledFirmware(installedFirmwareVersion)
        mockServerFirmware(networkFirmwareVersion)
        val firmwareUpdate = buildFirmwareUpdate(installedFirmwareVersion)
        val sut = FirmwareUpgraderGenII(eobrReader, eobrService, firmwareUpdate, fwImageDownloader, facadeFactory)

        val isUpdateRequired = sut.isFirmwareUpgradeRequired

        assertTrue(isUpdateRequired)
        assertEqual("Expected version to downgrade to is $networkFirmwareVersion",
                networkFirmwareVersion, sut.firmwareUpdateConfig.version)
    }

    @Test
    fun test6_88_113_MandateOff_NetworkReturnsMajorMinorDowngrade_ReturnsUpdateNeeded() {
        val installedFirmwareVersion = "7.1.11"
        val networkFirmwareVersion = "6.88.114"
        mockInstalledFirmware(installedFirmwareVersion)
        mockServerFirmware(networkFirmwareVersion)
        val firmwareUpdate = buildFirmwareUpdate(installedFirmwareVersion)
        val sut = FirmwareUpgraderGenII(eobrReader, eobrService, firmwareUpdate, fwImageDownloader, facadeFactory)

        val isUpdateRequired = sut.isFirmwareUpgradeRequired

        assertTrue(isUpdateRequired)
        assertEqual("Expected version to downgrade to is $networkFirmwareVersion",
                networkFirmwareVersion, sut.firmwareUpdateConfig.version)
    }

    private fun mockHardwareBundle(): Bundle {
        val bundle = mock(Bundle::class.java)
        `when`(bundle.getString(EobrEngineBase.EOBR_HARDWARE_VERSION)).thenReturn("2.5")
        return bundle
    }

    private fun mockServerFirmware(networkFirmwareVersion: String) {
        val (networkMajor, networkMinor, networkPatch) =
                networkFirmwareVersion.split(".").map { it.toInt() }

        val networkFirmware = FirmwareVersion().apply {
            major = networkMajor
            minor = networkMinor
            patch = networkPatch
        }

        val restServices = mock(IRESTWebServiceHelper::class.java)
        `when`(restServices.CheckForFirmwareUpdate(anyString(), anyInt(), anyInt()))
                .thenReturn(networkFirmware)
        RestWebServiceHelperFactory.setInstance(restServices)
        //Enable network
        `when`(networkInfo.isConnected).thenReturn(true)
    }

    private fun mockNoNetworkConnection() {
        `when`(networkInfo.isConnected).thenReturn(false)
    }

    private fun buildFirmwareUpdate(installedFirmwareVersion: String) : FirmwareUpdate {
        return FirmwareUpdate().apply {
            installedVersion = installedFirmwareVersion
            maker = Constants.JJKA
            generation = Constants.GENERATION_GEN_II
            version = installedFirmwareVersion
        }
    }
}