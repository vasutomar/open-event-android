package org.fossasia.openevent.general.social

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_social_link.view.*
import org.fossasia.openevent.general.R

class SocialLinksViewHolder(itemView: View, private var context: Context) : RecyclerView.ViewHolder(itemView) {

    fun bind(socialLink: SocialLink) {
        val drawableId = getSocialLinkDrawableId(socialLink.name)
        if (drawableId != -1) {
            val imageDrawable: Drawable? = ContextCompat.getDrawable(context, drawableId)
            imageDrawable?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, R.color.greyMore), PorterDuff.Mode.SRC_IN)

            itemView.img_social_link.setImageDrawable(imageDrawable)
        }

        itemView.setOnClickListener{
            setUpCustomTab(context, socialLink.link)
        }
    }

    private fun getSocialLinkDrawableId(name: String): Int {
        return when (name) {
            "Github Url" -> R.drawable.ic_github_24dp
            "Twitter Url" -> R.drawable.ic_twitter_24dp
            "Facebook Url" -> R.drawable.ic_facebook_24dp
            "LinkedIn Url" -> R.drawable.ic_linkedin_24dp
            "Youtube Url" -> R.drawable.ic_youtube_24dp
            "Google Url" -> R.drawable.ic_google_plus_24dp
            else -> -1
        }
    }

    private fun setUpCustomTab(context: Context, url: String) {
        var finalUrl = url
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            finalUrl = "http://$url"
        }

        CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .setCloseButtonIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_arrow_back_white_cct_24dp))
                .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right)
                .build()
                .launchUrl(context, Uri.parse(finalUrl))
    }
}