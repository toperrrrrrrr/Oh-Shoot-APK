package com.ohshootstudio.resibooth.viewmodel

import android.content.Context
import android.graphics.Bitmap
import com.ohshootstudio.resibooth.data.SettingsRepository
import com.ohshootstudio.resibooth.domain.AppSettings
import com.ohshootstudio.resibooth.ui.screens.LayoutType
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: SessionViewModel
    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { settingsRepository.settingsFlow } returns flowOf(AppSettings())
        viewModel = SessionViewModel(context, settingsRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startSession initializes state correctly`() {
        val photoCount = 4
        val layoutType = LayoutType.GRID_2X2
        
        viewModel.startSession(photoCount, layoutType)
        
        val state = viewModel.uiState.value
        assertEquals(photoCount, state.selectedLayout)
        assertEquals(layoutType, state.selectedLayoutType)
        assertEquals(photoCount, state.capturedPhotos.size)
        assertTrue(state.capturedPhotos.all { it == null })
        assertEquals(0, state.currentShotIndex)
        assertFalse(state.sessionComplete)
    }

    @Test
    fun `savePhoto updates index and completion status`() {
        val mockBitmap: Bitmap = mockk()
        viewModel.startSession(1, LayoutType.SINGLE)
        
        viewModel.savePhoto(0, mockBitmap)
        
        val state = viewModel.uiState.value
        assertEquals(1, state.currentShotIndex)
        assertTrue(state.sessionComplete)
        assertEquals(mockBitmap, state.capturedPhotos[0])
    }

    @Test
    fun `retakePhoto resets specific photo and index`() {
        val mockBitmap: Bitmap = mockk()
        viewModel.startSession(4, LayoutType.GRID_2X2)
        viewModel.savePhoto(0, mockBitmap)
        viewModel.savePhoto(1, mockBitmap)
        
        viewModel.retakePhoto(0)
        
        val state = viewModel.uiState.value
        assertEquals(0, state.currentShotIndex)
        assertFalse(state.sessionComplete)
        assertEquals(null, state.capturedPhotos[0])
        assertEquals(mockBitmap, state.capturedPhotos[1])
    }

    @Test
    fun `setCopyCount clamps values between 1 and 5`() {
        viewModel.setCopyCount(10)
        assertEquals(5, viewModel.uiState.value.copyCount)
        
        viewModel.setCopyCount(0)
        assertEquals(1, viewModel.uiState.value.copyCount)
        
        viewModel.setCopyCount(3)
        assertEquals(3, viewModel.uiState.value.copyCount)
    }

    @Test
    fun `updateSettings triggers repository save`() {
        val newSettings = AppSettings(isActivated = true)
        viewModel.updateSettings(newSettings)
        
        io.mockk.coVerify { settingsRepository.saveSettings(newSettings) }
    }
}

