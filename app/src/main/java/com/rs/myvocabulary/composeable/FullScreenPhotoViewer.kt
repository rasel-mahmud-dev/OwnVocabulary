package com.rs.learnmedia.composeable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenPhotoViewer(
        imageUrls: List<String>,
        initialPage: Int = 0,
        onDismiss: () -> Unit,
        onDownload: ((String) -> Unit)? = null,
        onShare: ((String) -> Unit)? = null
) {
        val pagerState =
                rememberPagerState(initialPage = initialPage, pageCount = { imageUrls.size })

        Dialog(
                onDismissRequest = onDismiss,
                properties =
                        DialogProperties(
                                usePlatformDefaultWidth = false,
                                decorFitsSystemWindows = false
                        )
        ) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                        // Photo Pager
                        HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize(),
                                key = { imageUrls[it] }
                        ) { page ->
                                var scale by remember { mutableFloatStateOf(1f) }
                                var offsetX by remember { mutableFloatStateOf(0f) }
                                var offsetY by remember { mutableFloatStateOf(0f) }

                                // Reset zoom and pan when page changes
                                LaunchedEffect(pagerState.currentPage) {
                                        if (pagerState.currentPage == page) {
                                                scale = 1f
                                                offsetX = 0f
                                                offsetY = 0f
                                        }
                                }

                                val transformableState =
                                        rememberTransformableState { zoomChange, panChange, _ ->
                                                val newScale =
                                                        (scale * zoomChange).coerceIn(0.5f, 5f)

                                                // Only allow pan when zoomed in
                                                if (newScale > 1f) {
                                                        scale = newScale
                                                        offsetX += panChange.x
                                                        offsetY += panChange.y
                                                } else {
                                                        scale = 1f
                                                        offsetX = 0f
                                                        offsetY = 0f
                                                }
                                        }

                                Box(
                                        modifier =
                                                Modifier.fillMaxSize()
                                                        .pointerInput(Unit) {
                                                                detectTapGestures(
                                                                        onDoubleTap = { tapOffset ->
                                                                                if (scale > 1f) {
                                                                                        // Reset
                                                                                        // zoom
                                                                                        scale = 1f
                                                                                        offsetX = 0f
                                                                                        offsetY = 0f
                                                                                } else {
                                                                                        // Zoom in
                                                                                        // to 2x at
                                                                                        // tap
                                                                                        // position
                                                                                        scale = 2.5f
                                                                                        // Center
                                                                                        // the zoom
                                                                                        // on tap
                                                                                        // location
                                                                                        offsetX =
                                                                                                (size.width /
                                                                                                        2 -
                                                                                                        tapOffset
                                                                                                                .x) *
                                                                                                        scale
                                                                                        offsetY =
                                                                                                (size.height /
                                                                                                        2 -
                                                                                                        tapOffset
                                                                                                                .y) *
                                                                                                        scale
                                                                                }
                                                                        }
                                                                )
                                                        }
                                                        // Only enable transformable when not at
                                                        // scale 1
                                                        .then(
                                                                if (scale >= 1f) {
                                                                        Modifier.transformable(
                                                                                state =
                                                                                        transformableState,
                                                                                lockRotationOnZoomPan =
                                                                                        true
                                                                        )
                                                                } else {
                                                                        Modifier
                                                                }
                                                        ),
                                        contentAlignment = Alignment.Center
                                ) {
                                        var isLoading by remember { mutableStateOf(true) }

                                        AsyncImage(
                                                model = imageUrls[page],
                                                contentDescription = "Photo ${page + 1}",
                                                modifier =
                                                        Modifier.fillMaxSize().graphicsLayer {
                                                                scaleX = scale
                                                                scaleY = scale
                                                                translationX = offsetX
                                                                translationY = offsetY
                                                        },
                                                contentScale = ContentScale.Fit,
                                                onLoading = { isLoading = true },
                                                onSuccess = { isLoading = false },
                                                onError = { isLoading = false }
                                        )

                                        if (isLoading) {
                                                CircularProgressIndicator(
                                                        modifier = Modifier.size(48.dp),
                                                        color = Color.White
                                                )
                                        }
                                }
                        }

                        // Top Bar with close button and counter
                        Surface(
                                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                                color = Color.Black.copy(alpha = 0.6f)
                        ) {
                                Row(
                                        modifier =
                                                Modifier.fillMaxWidth()
                                                        .padding(
                                                                horizontal = 8.dp,
                                                                vertical = 12.dp
                                                        )
                                                        .statusBarsPadding(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                ) {
                                        IconButton(onClick = onDismiss) {
                                                Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Close",
                                                        tint = Color.White
                                                )
                                        }

                                        Text(
                                                text =
                                                        "${pagerState.currentPage + 1} / ${imageUrls.size}",
                                                color = Color.White,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold
                                        )

                                        Row {
                                                if (onShare != null) {
                                                        IconButton(
                                                                onClick = {
                                                                        onShare(
                                                                                imageUrls[
                                                                                        pagerState
                                                                                                .currentPage]
                                                                        )
                                                                }
                                                        ) {
                                                                Icon(
                                                                        imageVector =
                                                                                Icons.Default.Share,
                                                                        contentDescription =
                                                                                "Share",
                                                                        tint = Color.White
                                                                )
                                                        }
                                                }

                                                if (onDownload != null) {
                                                        IconButton(
                                                                onClick = {
                                                                        onDownload(
                                                                                imageUrls[
                                                                                        pagerState
                                                                                                .currentPage]
                                                                        )
                                                                }
                                                        ) {
                                                                Icon(
                                                                        imageVector =
                                                                                Icons.Default
                                                                                        .Download,
                                                                        contentDescription =
                                                                                "Download",
                                                                        tint = Color.White
                                                                )
                                                        }
                                                }

                                                // Placeholder for symmetry when no actions
                                                if (onShare == null && onDownload == null) {
                                                        Spacer(modifier = Modifier.width(48.dp))
                                                }
                                        }
                                }
                        }

                        // Navigation Arrows
                        Box(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                contentAlignment = Alignment.Center
                        ) {
                                val scope = rememberCoroutineScope()

                                // Previous Button
                                if (pagerState.currentPage > 0) {
                                        IconButton(
                                                onClick = {
                                                        scope.launch {
                                                                pagerState.animateScrollToPage(
                                                                        pagerState.currentPage - 1
                                                                )
                                                        }
                                                },
                                                modifier =
                                                        Modifier.align(Alignment.CenterStart)
                                                                .background(
                                                                        color =
                                                                                Color.Black.copy(
                                                                                        alpha = 0.3f
                                                                                ),
                                                                        shape =
                                                                                androidx.compose
                                                                                        .foundation
                                                                                        .shape
                                                                                        .CircleShape
                                                                )
                                        ) {
                                                Icon(
                                                        imageVector = Icons.Default.ChevronLeft,
                                                        contentDescription = "Previous",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(32.dp)
                                                )
                                        }
                                }

                                // Next Button
                                if (pagerState.currentPage < imageUrls.size - 1) {
                                        IconButton(
                                                onClick = {
                                                        scope.launch {
                                                                pagerState.animateScrollToPage(
                                                                        pagerState.currentPage + 1
                                                                )
                                                        }
                                                },
                                                modifier =
                                                        Modifier.align(Alignment.CenterEnd)
                                                                .background(
                                                                        color =
                                                                                Color.Black.copy(
                                                                                        alpha = 0.3f
                                                                                ),
                                                                        shape =
                                                                                androidx.compose
                                                                                        .foundation
                                                                                        .shape
                                                                                        .CircleShape
                                                                )
                                        ) {
                                                Icon(
                                                        imageVector = Icons.Default.ChevronRight,
                                                        contentDescription = "Next",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(32.dp)
                                                )
                                        }
                                }
                        }

                        // Bottom indicator and info
                        Surface(
                                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                                color = Color.Black.copy(alpha = 0.6f)
                        ) {
                                Column(
                                        modifier = Modifier.padding(16.dp).navigationBarsPadding(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                        // Page indicator dots
                                        if (imageUrls.size > 1) {
                                                Row(
                                                        horizontalArrangement =
                                                                Arrangement.spacedBy(8.dp),
                                                        verticalAlignment =
                                                                Alignment.CenterVertically
                                                ) {
                                                        repeat(imageUrls.size.coerceAtMost(10)) {
                                                                index ->
                                                                Box(
                                                                        modifier =
                                                                                Modifier.size(
                                                                                                if (pagerState
                                                                                                                .currentPage ==
                                                                                                                index
                                                                                                )
                                                                                                        8.dp
                                                                                                else
                                                                                                        6.dp
                                                                                        )
                                                                                        .background(
                                                                                                color =
                                                                                                        if (pagerState
                                                                                                                        .currentPage ==
                                                                                                                        index
                                                                                                        )
                                                                                                                Color.White
                                                                                                        else
                                                                                                                Color.White
                                                                                                                        .copy(
                                                                                                                                alpha =
                                                                                                                                        0.4f
                                                                                                                        ),
                                                                                                shape =
                                                                                                        MaterialTheme
                                                                                                                .shapes
                                                                                                                .small
                                                                                        )
                                                                )
                                                        }

                                                        // Show +N if more than 10 images
                                                        if (imageUrls.size > 10) {
                                                                Text(
                                                                        text =
                                                                                "+${imageUrls.size - 10}",
                                                                        color =
                                                                                Color.White.copy(
                                                                                        alpha = 0.7f
                                                                                ),
                                                                        style =
                                                                                MaterialTheme
                                                                                        .typography
                                                                                        .bodySmall
                                                                )
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
}
