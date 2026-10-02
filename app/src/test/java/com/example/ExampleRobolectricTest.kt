package com.example

import android.content.Context
import androidx.media3.common.Format
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.ApiClient
import com.example.data.models.APIMatch
import com.example.data.models.MatchSource
import com.example.data.models.MatchTeams
import com.example.data.models.SportCategory
import com.example.data.models.TeamInfo
import com.example.player.VideoQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AR SPORTS", appName)
    }

    @Test
    fun `test ApiClient url helpers`() {
        val badgeUrl = ApiClient.getBadgeUrl("arsenal")
        assertEquals("https://streamed.pk/api/images/badge/arsenal.webp", badgeUrl)

        val posterUrl = ApiClient.getPosterUrl("/posters/arsenal-vs-chelsea")
        assertEquals("https://streamed.pk/posters/arsenal-vs-chelsea.webp", posterUrl)
    }

    @Test
    fun `test APIMatch data structure`() {
        val match = APIMatch(
            id = "match-123",
            title = "Arsenal vs Chelsea",
            category = "soccer",
            date = 1700000000000L,
            poster = "/poster.webp",
            popular = true,
            teams = MatchTeams(
                home = TeamInfo("Arsenal", "arsenal"),
                away = TeamInfo("Chelsea", "chelsea")
            ),
            sources = listOf(MatchSource("alpha", "alpha-id-1"))
        )

        assertEquals("match-123", match.id)
        assertEquals("Arsenal", match.teams?.home?.name)
        assertEquals("Chelsea", match.teams?.away?.name)
        assertEquals(1, match.sources.size)
    }

    @Test
    fun `test VideoQuality labels`() {
        val format1080 = Format.Builder()
            .setWidth(1920)
            .setHeight(1080)
            .setAverageBitrate(4500000)
            .build()
        val quality1080 = VideoQuality.fromFormat(format1080)
        assertEquals("1080p", quality1080.label)
        assertEquals(1920, quality1080.width)
        assertEquals(1080, quality1080.height)

        val format720 = Format.Builder()
            .setWidth(1280)
            .setHeight(720)
            .build()
        val quality720 = VideoQuality.fromFormat(format720)
        assertEquals("720p", quality720.label)

        assertEquals("Auto", VideoQuality.AUTO.label)
    }

    @Test
    fun `test SportCategory system`() {
        assertTrue(SportCategory.CATEGORIES.isNotEmpty())
        val football = SportCategory.findBySlug("football")
        assertEquals("FOOTBALL", football.name)
        val cricket = SportCategory.findBySlug("cricket")
        assertEquals("CRICKET", cricket.name)
    }
}
