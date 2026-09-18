package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.PicsumImage
import com.example.data.repository.AuthorFilter
import com.example.data.repository.GalleryRepository
import com.example.data.repository.UserRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("LensGallery", appName)
  }

  @Test
  fun `registration validation validates email and phone length`() {
    // Valid registration
    val valid = UserRepository.validateRegistration(
      fullName = "John Doe",
      email = "john@example.com",
      gender = "Male",
      mobileNumber = "1234567890",
      address = "123 Main St",
      city = "New York",
      password = "secretpassword",
      confirmPassword = "secretpassword"
    )
    assertNull(valid)

    // Invalid mobile (less than 10 digits)
    val invalidPhone = UserRepository.validateRegistration(
      fullName = "John Doe",
      email = "john@example.com",
      gender = "Male",
      mobileNumber = "12345",
      address = "123 Main St",
      city = "New York",
      password = "secretpassword",
      confirmPassword = "secretpassword"
    )
    assertNotNull(invalidPhone)

    // Password mismatch
    val mismatch = UserRepository.validateRegistration(
      fullName = "John Doe",
      email = "john@example.com",
      gender = "Male",
      mobileNumber = "1234567890",
      address = "123 Main St",
      city = "New York",
      password = "secretpassword1",
      confirmPassword = "secretpassword2"
    )
    assertNotNull(mismatch)
  }

  @Test
  fun `filterImages correctly filters author range`() {
    val sampleImages = listOf(
      PicsumImage("1", "Alejandro Escamilla", 100, 100, "url1", "d1"),
      PicsumImage("2", "Danielle MacInnes", 100, 100, "url2", "d2"),
      PicsumImage("3", "Paul Jarvis", 100, 100, "url3", "d3"),
      PicsumImage("4", "Vadim Sherbakov", 100, 100, "url4", "d4")
    )

    val aToM = GalleryRepository.filterImages(sampleImages, "", AuthorFilter.A_TO_M)
    assertEquals(2, aToM.size)
    assertEquals("Alejandro Escamilla", aToM[0].author)
    assertEquals("Danielle MacInnes", aToM[1].author)

    val nToZ = GalleryRepository.filterImages(sampleImages, "", AuthorFilter.N_TO_Z)
    assertEquals(2, nToZ.size)
    assertEquals("Paul Jarvis", nToZ[0].author)
    assertEquals("Vadim Sherbakov", nToZ[1].author)

    val searchResult = GalleryRepository.filterImages(sampleImages, "paul", AuthorFilter.ALL)
    assertEquals(1, searchResult.size)
    assertEquals("Paul Jarvis", searchResult[0].author)
  }
}
