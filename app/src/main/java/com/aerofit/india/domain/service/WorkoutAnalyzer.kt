package com.aerofit.india.domain.service

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlin.math.abs
import kotlin.math.atan2

enum class ExerciseType { SQUAT, PUSHUP }

// The Upgraded AI Brain! Tracks both Pushups and Squats.
class WorkoutAnalyzer(
    private val exerciseType: ExerciseType,
    private val onRepDetected: () -> Unit
) : ImageAnalysis.Analyzer {

    private val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()
    private val poseDetector = PoseDetection.getClient(options)

    private var isDown = false

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            poseDetector.process(image)
                .addOnSuccessListener { pose ->
                    if (exerciseType == ExerciseType.SQUAT) {
                        // --- SQUAT TRACKING (Hip, Knee, Ankle) ---
                        val hip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
                        val knee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
                        val ankle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)

                        // The Likelihood check makes it much more accurate!
                        if (hip != null && knee != null && ankle != null &&
                            hip.inFrameLikelihood > 0.5f && knee.inFrameLikelihood > 0.5f) {

                            val angle = calculateAngle(hip, knee, ankle)

                            // More forgiving angles so it catches reps better!
                            if (angle < 115.0) isDown = true
                            else if (angle > 150.0 && isDown) {
                                isDown = false
                                onRepDetected()
                            }
                        }
                    } else {
                        // --- PUSH-UP TRACKING (Shoulder, Elbow, Wrist) ---
                        val shoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                        val elbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
                        val wrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)

                        if (shoulder != null && elbow != null && wrist != null &&
                            shoulder.inFrameLikelihood > 0.5f && elbow.inFrameLikelihood > 0.5f) {

                            val angle = calculateAngle(shoulder, elbow, wrist)

                            if (angle < 100.0) isDown = true
                            else if (angle > 150.0 && isDown) {
                                isDown = false
                                onRepDetected()
                            }
                        }
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
                .addOnFailureListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }

    private fun calculateAngle(first: PoseLandmark, mid: PoseLandmark, last: PoseLandmark): Double {
        var result = Math.toDegrees(
            atan2(last.position.y - mid.position.y, last.position.x - mid.position.x).toDouble() -
                    atan2(first.position.y - mid.position.y, first.position.x - mid.position.x).toDouble()
        )
        result = abs(result)
        if (result > 180) result = 360.0 - result
        return result
    }
}