package com.rs.ownvocabulary.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navHostController: NavHostController) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "About VocabBook",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navHostController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            // Header Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "VocabBook",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Expand your vocabulary",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // About Description
            SectionCard(
                title = "About VocabBook",
                content = "VocabBook is a modern vocabulary-building application designed to help you collect, learn, and practice new words with the assistance of AI.\n\nIt is available on Web, Mobile, and as a Browser Extension, making it easy to grow your vocabulary anytime, anywhere."
            )

            // Key Features Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Key Features",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FeatureSection(
                    icon = Icons.Outlined.PhoneAndroid,
                    title = "Mobile Application",
                    subtitle = "Android – Kotlin Jetpack Compose",
                    features = listOf(
                        "Built with Kotlin Jetpack Compose for a smooth and native experience",
                        "Offline mode to access your saved words without internet connectivity",
                        "Add new words directly from the Share option in other apps or browsers",
                        "AI-powered assistance for meanings, synonyms, antonyms, and examples",
                        "Clean and modern user interface for an engaging learning experience"
                    )
                )

                FeatureSection(
                    icon = Icons.Outlined.Language,
                    title = "Web Application",
                    subtitle = "React + Node.js + Express.js",
                    features = listOf(
                        "React.js frontend for a fast and responsive experience",
                        "Node.js + Express.js backend for reliability and performance",
                        "Dark Mode for better readability in low-light environments",
                        "Dashboard to view progress, statistics, and achievements",
                        "Intuitive and seamless learning journey across devices"
                    )
                )

                FeatureSection(
                    icon = Icons.Outlined.Extension,
                    title = "Browser Extension",
                    subtitle = "Quick-save functionality",
                    features = listOf(
                        "Save new words directly from web pages while browsing",
                        "Quick-save functionality to add words instantly to your VocabBook",
                        "Convenient to capture vocabulary in real time while reading online"
                    )
                )

                FeatureSection(
                    icon = Icons.Outlined.BookmarkBorder,
                    title = "Word Management",
                    subtitle = "Organize and categorize",
                    features = listOf(
                        "Save and organize words with both short and detailed meanings",
                        "Add synonyms, antonyms, and example sentences",
                        "Assign proficiency levels: Beginner, Intermediate, and Advanced",
                        "Use custom tags to categorize and group words",
                        "Personalized vocabulary library that grows with your progress"
                    )
                )

                FeatureSection(
                    icon = Icons.Outlined.Explore,
                    title = "Discovery and Search",
                    subtitle = "Learn from the community",
                    features = listOf(
                        "Explore new words shared by other users in the Discovery Section",
                        "Learn from trending or uncommon words within the community",
                        "Advanced search options by name, meaning, tags, or proficiency level",
                        "Continuous learning from the wider user community"
                    )
                )

                FeatureSection(
                    icon = Icons.Outlined.Security,
                    title = "Authentication",
                    subtitle = "Secure and synced",
                    features = listOf(
                        "Sign up or log in using Google, GitHub, or Email and password",
                        "Words are safely stored and synced across devices",
                        "Consistent and secure experience across all platforms"
                    )
                )
            }

            // Why Choose Section
            SectionCard(
                title = "Why Choose VocabBook?",
                content = "• AI-powered learning with instant meanings, synonyms, antonyms, and examples\n" +
                        "• Cross-platform availability across web, mobile, and browser extension\n" +
                        "• Personalized dashboard to track learning progress and achievements\n" +
                        "• Community-driven discovery of new and useful vocabulary\n" +
                        "• Productivity-focused tools to save and organize words effortlessly\n\n" +
                        "With VocabBook, expanding your vocabulary goes beyond memorization. It provides a structured, personalized, and engaging way to strengthen your language skills while adapting to your learning needs."
            )

            // Footer
            Text(
                text = "© 2025 Rasel Mahmud. All rights reserved.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5f
            )
        }
    }
}

@Composable
fun FeatureSection(
    icon: ImageVector,
    title: String,
    subtitle: String,
    features: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Features List
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                features.forEach { feature ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4f,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}