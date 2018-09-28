package com.jjkeller.kmbapi.controller.EOBR

import com.jjkeller.kmbapi.enums.DatabusTypeEnum
import com.jjkeller.kmbapi.kmbeobr.Constants
import com.spun.util.Asserts.assertEqual
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.mockito.Matchers.any
import org.mockito.Mockito.`when`

class FirmwareUpgraderFactoryTest : FirmwareTestBase() {
    @Test
    fun testGenII_6_88_0_NonFastBus_MandateOff_NoNetwork_ReturnsDefaultUpgrader() {
        setupGenIIDevice()
        val installedFirmware = "6.88.0"
        mockInstalledFirmware(installedFirmware)

        val sut = FirmwareUpgraderFactory.GetFirmwareUpgrader(eobrReader, eobrService)

        assertTrue(sut.firmwareUpdateConfig.defaultVersion)
        assertFalse(sut.firmwareUpdateConfig.forceUpdate)
    }

    @Test
    fun testGenII_6_88_0_FastBus_MandateOff_NoNetwork_ReturnsUpdateNeededToMinAobrdFastBusVersion() {
        setupGenIIDevice()
        val installedFirmware = "6.88.0"
        mockInstalledFirmware(installedFirmware)
        val fastDatabus = DatabusTypeEnum(DatabusTypeEnum.DUALMODEJ1708J1939F)

        val sut = FirmwareUpgraderFactory.GetFirmwareUpgrader(eobrReader, eobrService, fastDatabus)

        assertTrue(sut.firmwareUpdateConfig.forceUpdate)
        val minFastBusVersion = FirmwareUpgraderFactory.MIN_FAST_COMPLIANT_VERSION
        assertEqual("Expected version to update to minimum fast bus version $minFastBusVersion",
                minFastBusVersion, sut.firmwareUpdateConfig.version)
    }

    @Test
    fun testGenII_6_89_0_FastBus_MandateOff_NoNetwork_ReturnsUpdateNeededToMinAobrdFastBusVersion() {
        setupGenIIDevice()
        val installedFirmware = "6.89.0"
        mockInstalledFirmware(installedFirmware)
        val fastDatabus = DatabusTypeEnum(DatabusTypeEnum.DUALMODEJ1708J1939F)

        val sut = FirmwareUpgraderFactory.GetFirmwareUpgrader(eobrReader, eobrService, fastDatabus)

        assertTrue(sut.firmwareUpdateConfig.forceUpdate)
        val minFastBusVersion = FirmwareUpgraderFactory.MIN_FAST_COMPLIANT_VERSION
        assertEqual("Expected version to update to minimum fast bus version $minFastBusVersion",
                minFastBusVersion, sut.firmwareUpdateConfig.version)
    }

    @Test
    fun testGenII_6_89_0_NonFastBus_MandateOff_NoNetwork_ReturnsUpdatedNeededToMinAobrFastBusVersion() {
        setupGenIIDevice()
        val installedFirmware = "6.89.0"
        mockInstalledFirmware(installedFirmware)

        val sut = FirmwareUpgraderFactory.GetFirmwareUpgrader(eobrReader, eobrService)

        assertTrue(sut.firmwareUpdateConfig.forceUpdate)
        val minFastBusVersion = FirmwareUpgraderFactory.MIN_FAST_COMPLIANT_VERSION
        assertEqual("Expected version to update to minimum fast bus version $minFastBusVersion",
                minFastBusVersion, sut.firmwareUpdateConfig.version)
    }

    @Test
    fun testGenII_6_89_0_FastBus_MandateOn_NoNetwork_ReturnsUpdateNeededToMinMandateFastBusVersion() {
        setupGenIIDevice()
        val installedFirmware = "6.89.0"
        enableMandate()
        mockInstalledFirmware(installedFirmware)
        val fastDatabus = DatabusTypeEnum(DatabusTypeEnum.DUALMODEJ1708J1939F)

        val sut = FirmwareUpgraderFactory.GetFirmwareUpgrader(eobrReader, eobrService, fastDatabus)

        assertTrue(sut.firmwareUpdateConfig.forceUpdate)
        val minFastBusVersion = FirmwareUpgraderFactory.MIN_MANDATE_FAST_COMPLIANT_VERSION
        assertEqual("Expected version to update to minimum fast bus version $minFastBusVersion",
        minFastBusVersion, sut.firmwareUpdateConfig.version)
    }

    @Test
    fun testGenII_6_89_0_NonFastBus_MandateOn_NoNetwork_ReturnsUpdatedNeededToMinMandateFastBusVersion() {
        setupGenIIDevice()
        val installedFirmware = "6.89.0"
        enableMandate()
        mockInstalledFirmware(installedFirmware)

        val sut = FirmwareUpgraderFactory.GetFirmwareUpgrader(eobrReader, eobrService)

        assertTrue(sut.firmwareUpdateConfig.forceUpdate)
        val minFastBusVersion = FirmwareUpgraderFactory.MIN_MANDATE_FAST_COMPLIANT_VERSION
        assertEqual("Expected version to update to minimum fast bus version $minFastBusVersion",
        minFastBusVersion, sut.firmwareUpdateConfig.version)
    }

    @Test
    fun testGenII_6_88_113_NonFastBus_MandateOff_NoNetwork_ReturnsDefaultUpgrader() {
        setupGenIIDevice()
        val installedFirmware = "6.88.113"
        mockInstalledFirmware(installedFirmware)

        val sut = FirmwareUpgraderFactory.GetFirmwareUpgrader(eobrReader, eobrService)

        assertTrue(sut.firmwareUpdateConfig.defaultVersion)
        assertFalse(sut.firmwareUpdateConfig.forceUpdate)
    }

    private fun setupGenIIDevice(isJJKHardware: Boolean = true) {
        `when`(eobrReader.eobrGeneration).thenReturn(Constants.GENERATION_GEN_II)

        `when`(eobrReader.Technician_GetEobrHardware(any())).thenReturn(isJJKHardware)
    }

}