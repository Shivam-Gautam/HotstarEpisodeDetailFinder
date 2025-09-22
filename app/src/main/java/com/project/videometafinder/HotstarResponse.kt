package com.project.videometafinder

import com.google.gson.annotations.SerializedName

data class HotstarResponse(
    @SerializedName("success") val success: SuccessData?
)

data class SuccessData(
    @SerializedName("space") val space: SpaceData?
)

data class SpaceData(
    @SerializedName("id") val id: String?,
    @SerializedName("template") val template: String?,
    @SerializedName("widget_wrappers") val widgetWrappers: List<WidgetWrapper>?
)

data class WidgetWrapper(
    @SerializedName("template") val template: String?,
    @SerializedName("widget") val widget: CategoryTrayWidget?,
    @SerializedName("id") val id: String?
)

data class CategoryTrayWidget(
    @SerializedName("@type") val type: String?,
    @SerializedName("data") val data: CategoryTrayWidgetData?
)

data class CategoryTrayWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("tray_items") val trayItems: TrayItems?
)

data class TrayItems(
    @SerializedName("data") val data: TrayItemsData?
)

data class TrayItemsData(
    @SerializedName("items") val items: List<PlayableContentItem>?
)

data class PlayableContentItem(
    @SerializedName("playable_content") val playableContent: PlayableContent?
)

data class PlayableContent(
    @SerializedName("data") val data: PlayableContentData?
)

data class PlayableContentData(
    @SerializedName("poster") val poster: PosterData?,
    @SerializedName("title") val title: String?,
    @SerializedName("tags") val tags: List<Tag>?,
    @SerializedName("description") val description: String?,
    @SerializedName("content_id") val contentId: String?,
    @SerializedName("cw_info") val cwInfo: CwInfo?
)

data class PosterData(
    @SerializedName("src") val src: String?,
    @SerializedName("alt") val alt: String?
)

data class Tag(
    @SerializedName("value") val value: String?
)

data class CwInfo(
    @SerializedName("content_id") val contentId: String?,
    @SerializedName("parent_content_id") val parentContentId: String?
)