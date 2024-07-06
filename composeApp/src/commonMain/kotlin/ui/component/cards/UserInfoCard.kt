package ui.component.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import copyToClipboard
import logic.model.HomeFeedResponse
import ui.component.KamelLoader
import ui.ffflist.FFFListType
import util.DateUtils.fromToday
import util.DeviceUtil.isLogin
import util.noRippleClickable

/**
 * Created by bggRGjQaUbCoE on 2024/6/4
 */

@Composable
fun UserInfoCard(
    data: HomeFeedResponse.Data,
    onFollow: (String, Int) -> Unit,
    onPMUser: (String, String) -> Unit,
    onViewFFFList: (String?, String, String?, String?) -> Unit,
    onViewImage: (List<String>, Int) -> Unit,
) {

    ConstraintLayout {

        val (cover, avatar, username, uidLevel, bio, lff, active, pm, followBtn) = createRefs()

        KamelLoader(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .constrainAs(cover) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                }
                .clickable {
                    onViewImage(listOf(data.cover.orEmpty()), 0)
                },
            url = data.cover.orEmpty(),
        )

        KamelLoader(
            modifier = Modifier
                .padding(start = 20.dp, top = 110.dp)
                .height(80.dp)
                .width(80.dp)
                .clip(CircleShape)
                .constrainAs(avatar) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                }
                .clickable {
                    onViewImage(listOf(data.userAvatar.orEmpty()), 0)
                }
                .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
            url = data.userAvatar.orEmpty(),
        )

        Text(
            text = data.username.orEmpty(),
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 5.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .constrainAs(username) {
                    start.linkTo(parent.start)
                    top.linkTo(avatar.bottom)
                }
                .noRippleClickable {
                    copyToClipboard(data.username.orEmpty())
                }
        )

        Row(
            modifier = Modifier
                .padding(start = 20.dp, top = 5.dp)
                .constrainAs(uidLevel) {
                    start.linkTo(parent.start)
                    top.linkTo(username.bottom)
                }
        ) {
            Text(
                text = "uid: ${data.uid}",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .noRippleClickable {
                        copyToClipboard(data.uid.orEmpty())
                    }
            )

            Text(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 6.dp),
                text = "Lv.${data.level}",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        if (!data.bio.isNullOrEmpty()) {
            Text(
                text = data.bio,
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(top = 5.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .constrainAs(bio) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(uidLevel.bottom)
                    }
                    .noRippleClickable {
                        copyToClipboard(data.bio)
                    },
            )
        }

        Row(
            modifier = Modifier
                .padding(start = 20.dp, top = 5.dp)
                .constrainAs(lff) {
                    start.linkTo(parent.start)
                    top.linkTo(if (data.bio.isNullOrEmpty()) uidLevel.bottom else bio.bottom)
                }
        ) {
            Text(
                text = "${data.feed?.id ?: 0}动态",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                modifier = Modifier.padding(start = 10.dp),
                text = "${data.beLikeNum}赞",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .noRippleClickable {
                        onViewFFFList(
                            data.uid.orEmpty(),
                            FFFListType.USER_FOLLOW.name,
                            null,
                            null
                        )
                    },
                text = "${data.follow}关注",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .noRippleClickable {
                        onViewFFFList(
                            data.uid.orEmpty(),
                            FFFListType.FAN.name,
                            null,
                            null
                        )
                    },
                text = "${data.fans}粉丝",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Text(
            text = "${fromToday(data.logintime ?: 0)}活跃",
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 5.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .constrainAs(active) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(lff.bottom)
                },
        )

        FilledTonalButton(
            onClick = {
                if (isLogin) {
                    onFollow(data.uid.orEmpty(), data.isFollow ?: 0)
                }
            },
            modifier = Modifier
                .padding(top = 10.dp, end = 20.dp)
                .constrainAs(followBtn) {
                    end.linkTo(parent.end)
                    top.linkTo(cover.bottom)
                }
        ) {
            Text(text = if (data.isFollow == 1) "取消关注" else "关注")
        }

        OutlinedIconButton(
            onClick = {
               // if (isLogin) {
                    onPMUser(data.uid.orEmpty(), data.username.orEmpty())
               // }
            },
            modifier = Modifier
                .padding(top = 10.dp, end = 10.dp)
                .constrainAs(pm) {
                    top.linkTo(followBtn.top)
                    bottom.linkTo(followBtn.bottom)
                    end.linkTo(followBtn.start)
                },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Icon(
                imageVector = Icons.Outlined.Mail,
                contentDescription = null
            )
        }

    }

}