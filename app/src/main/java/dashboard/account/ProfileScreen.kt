package com.example.stismartlib.ui.dashboard.account

import com.composables.icons.lucide.*

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.stismartlib.data.models.AvatarApprovalStatus
import com.example.stismartlib.data.rememberLibraryRepository
import com.example.stismartlib.ui.auth.UserType
import com.example.stismartlib.ui.components.ElmsBanner
import com.example.stismartlib.ui.components.ElmsBannerTone
import com.example.stismartlib.ui.components.ElmsLoadingBlock
import com.example.stismartlib.ui.components.PageHeader
import com.example.stismartlib.ui.components.PortalButton
import com.example.stismartlib.ui.components.PortalButtonVariant
import com.example.stismartlib.ui.components.PortalField
import com.example.stismartlib.ui.components.SectionCard
import com.example.stismartlib.ui.components.StatusBadge
import com.example.stismartlib.ui.theme.StiMarkBlue
import com.example.stismartlib.ui.theme.StiYellow
import kotlinx.coroutines.launch

private const val MAX_AVATAR_BYTES = 2 * 1024 * 1024
private val ALLOWED_AVATAR_MIME = setOf("image/png", "image/jpeg", "image/webp")

@Composable
fun ProfileScreen() {
    val repository = rememberLibraryRepository()
    val profile = repository.profile
    val avatar = repository.profileAvatar
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isStudent = repository.session.userType == UserType.STUDENT

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var programStrand by remember { mutableStateOf("") }
    var yearGradeLevel by remember { mutableStateOf("") }
    var localValidationError by remember { mutableStateOf<String?>(null) }
    var saveSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        runCatching { repository.refreshProfileScreen() }
    }

    LaunchedEffect(profile) {
        val loaded = profile ?: return@LaunchedEffect
        firstName = loaded.firstName
        lastName = loaded.lastName
        programStrand = loaded.programStrand
        yearGradeLevel = loaded.yearGradeLevel
    }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            localValidationError = null
            saveSuccess = false
            val mime = context.contentResolver.getType(uri).orEmpty()
            if (mime !in ALLOWED_AVATAR_MIME) {
                localValidationError = "Use a PNG, JPEG, or WebP image."
                return@launch
            }
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes == null) {
                localValidationError = "Could not read the selected image."
                return@launch
            }
            if (bytes.size > MAX_AVATAR_BYTES) {
                localValidationError = "Image must be 2 MB or smaller."
                return@launch
            }
            val ext = when (mime) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            runCatching {
                repository.uploadProfileAvatar(bytes, mime, "avatar.$ext")
            }
        }
    }

    val loading = repository.isLoading && profile == null
    val loadError = repository.lastError.takeIf { profile == null }
    val actionError = repository.profileActionError ?: localValidationError
    val roleLabel = UserType.fromServerRole(profile?.role ?: repository.session.userType.name).displayName
    val displayName = profile?.fullName?.takeIf { it.isNotBlank() }
        ?: listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
            .ifBlank { repository.session.userName }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            PageHeader(eyebrow = "My account", title = "My profile")
        }

        if (loadError != null) {
            item { ElmsBanner(message = loadError, tone = ElmsBannerTone.Error) }
        }
        if (actionError != null) {
            item { ElmsBanner(message = actionError, tone = ElmsBannerTone.Error) }
        }
        if (saveSuccess) {
            item {
                ElmsBanner(
                    message = "Profile saved. Your display name is updated.",
                    tone = ElmsBannerTone.Success,
                )
            }
        }

        when {
            loading -> item { ElmsLoadingBlock() }
            else -> {
                item {
                    SectionCard {
                        Text(
                            displayName.ifBlank { "Your profile" },
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        StatusBadge(roleLabel)
                        val status = profile?.status?.takeIf { it.isNotBlank() }
                        if (status != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                status,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 13.sp,
                            )
                        }
                    }
                }

                item {
                    SectionCard {
                        Text(
                            "Profile photo",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            ProfileAvatarVisual(
                                displayName = displayName,
                                imageUrl = avatar?.imageUrl,
                                status = avatar?.status ?: AvatarApprovalStatus.NONE,
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        when (avatar?.status) {
                            AvatarApprovalStatus.PENDING -> {
                                ElmsBanner(
                                    message = avatar.note
                                        ?: "Photo submitted. It stays private until a librarian approves it.",
                                    tone = ElmsBannerTone.Info,
                                )
                            }
                            AvatarApprovalStatus.REJECTED -> {
                                ElmsBanner(
                                    message = avatar.rejectionReason
                                        ?: avatar.note
                                        ?: "Photo was rejected. Choose a new image and submit again.",
                                    tone = ElmsBannerTone.Error,
                                )
                            }
                            AvatarApprovalStatus.APPROVED -> {
                                Text(
                                    "Your approved photo is shown above.",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontSize = 13.sp,
                                )
                            }
                            else -> {
                                Text(
                                    "Photos stay private until librarian approval. PNG, JPEG, or WebP up to 2 MB.",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontSize = 13.sp,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        PortalButton(
                            text = if (avatar?.status == AvatarApprovalStatus.NONE) {
                                "Submit for approval"
                            } else {
                                "Change photo"
                            },
                            onClick = {
                                localValidationError = null
                                saveSuccess = false
                                picker.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly,
                                    ),
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            variant = PortalButtonVariant.Secondary,
                            enabled = !repository.isAvatarUploading,
                            loading = repository.isAvatarUploading,
                            leadingIcon = Lucide.Camera,
                        )
                    }
                }

                item {
                    SectionCard {
                        Text(
                            "Editable details",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PortalField(
                            label = "First name",
                            value = firstName,
                            onValueChange = { firstName = it; saveSuccess = false },
                            placeholder = "First name",
                            leadingIcon = Lucide.User,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PortalField(
                            label = "Last name",
                            value = lastName,
                            onValueChange = { lastName = it; saveSuccess = false },
                            placeholder = "Last name",
                            leadingIcon = Lucide.User,
                        )
                        if (isStudent) {
                            Spacer(modifier = Modifier.height(12.dp))
                            PortalField(
                                label = "Program / strand",
                                value = programStrand,
                                onValueChange = { programStrand = it; saveSuccess = false },
                                placeholder = "Program or strand",
                                leadingIcon = Lucide.GraduationCap,
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            PortalField(
                                label = "Year / grade level",
                                value = yearGradeLevel,
                                onValueChange = { yearGradeLevel = it; saveSuccess = false },
                                placeholder = "Year or grade",
                                leadingIcon = Lucide.BookOpen,
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        PortalButton(
                            text = "Save",
                            onClick = {
                                scope.launch {
                                    localValidationError = null
                                    saveSuccess = false
                                    runCatching {
                                        repository.saveProfile(
                                            firstName = firstName,
                                            lastName = lastName,
                                            programStrand = if (isStudent) programStrand else null,
                                            yearGradeLevel = if (isStudent) yearGradeLevel else null,
                                        )
                                    }.onSuccess { saveSuccess = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !repository.isProfileSaving && firstName.isNotBlank() && lastName.isNotBlank(),
                            loading = repository.isProfileSaving,
                            leadingIcon = Lucide.Save,
                        )
                    }
                }

                item {
                    SectionCard {
                        Text(
                            "Account (read-only)",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PortalField(
                            label = "School ID",
                            value = profile?.schoolId ?: repository.session.schoolId,
                            onValueChange = {},
                            placeholder = "School ID",
                            leadingIcon = Lucide.IdCard,
                            enabled = false,
                            readOnly = true,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PortalField(
                            label = "Email",
                            value = profile?.email.orEmpty(),
                            onValueChange = {},
                            placeholder = "Email",
                            leadingIcon = Lucide.Mail,
                            enabled = false,
                            readOnly = true,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        PortalField(
                            label = "Role",
                            value = roleLabel,
                            onValueChange = {},
                            placeholder = "Role",
                            leadingIcon = Lucide.Shield,
                            enabled = false,
                            readOnly = true,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileAvatarVisual(
    displayName: String,
    imageUrl: String?,
    status: AvatarApprovalStatus,
) {
    val showPhoto = !imageUrl.isNullOrBlank() && status != AvatarApprovalStatus.REJECTED
    if (showPhoto) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .crossfade(true)
                .build(),
            contentDescription = "Profile photo",
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    } else {
        val initials = displayName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifBlank { "ST" }
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(StiYellow),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                initials,
                color = StiMarkBlue,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp,
            )
        }
    }
}
