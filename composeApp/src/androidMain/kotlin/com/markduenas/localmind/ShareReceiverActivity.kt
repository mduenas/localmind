package com.markduenas.localmind

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.markduenas.localmind.data.repository.CaptureRepository
import com.markduenas.localmind.domain.model.Capture
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.time.Clock
import org.koin.android.ext.android.inject

class ShareReceiverActivity : ComponentActivity() {
    private val captureRepository: CaptureRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                CoroutineScope(Dispatchers.IO).launch {
                    captureRepository.save(
                        Capture(
                            id = uuid4().toString(),
                            rawText = sharedText,
                            audioPath = null,
                            createdAt = Clock.System.now(),
                            processed = false,
                        )
                    )
                }

                val launchIntent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    data = android.net.Uri.parse("localmind://capture")
                }
                startActivity(launchIntent)
            }
        }

        finish()
    }
}
