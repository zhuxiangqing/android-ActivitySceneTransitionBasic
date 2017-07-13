/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.activityscenetransitionbasic

import com.squareup.picasso.Picasso

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.transition.Transition
import android.widget.ImageView
import kotlinx.android.synthetic.main.details.*

/**
 * Our secondary Activity which is launched from [MainActivity]. Has a simple detail UI
 * which has a large banner image, title and body text.
 */
class DetailActivity : Activity() {


    private var mItem: Item? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.details)
        // Retrieve the correct Item instance, using the ID provided in the Intent
        mItem = Item.getItem(intent.getIntExtra(EXTRA_PARAM_ID, 0))
        loadItem()

        // BEGIN_INCLUDE(detail_set_view_name)
        /**
         * Set the name of the view's which will be transition to, using the static values above.
         * This could be done in the layout XML, but exposing it via static variables allows easy
         * querying from other Activities
         */
        ViewCompat.setTransitionName(imageview_header, VIEW_NAME_HEADER_IMAGE)
        ViewCompat.setTransitionName(textview_title, VIEW_NAME_HEADER_TITLE)
        // END_INCLUDE(detail_set_view_name)


    }

    /**
     * 加载了图片和title文字
     * 不调用的话 动画会存在但是view上面没有内容
     */
    private fun loadItem() {
        // Set the title TextView to the item's name and author
        textview_title.text = getString(R.string.image_header, mItem!!.name, mItem!!.author, mItem!!.name)
        //如果我们只加载缩略图 并不会影响动画
        //不会有闪烁现象发生 但是可能会影响图片的质量
        loadThumbnail()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // If we're running on Lollipop and we have added a listener to the shared element
            // transition, load the thumbnail. The listener will load the full-size image when
            // the transition is complete.
//            loadThumbnail()
            addTransitionListener()
        } else {
            // If all other cases we should just load the full-size image now
            loadFullSizeImage()
        }
    }

    /**
     * Load the item's thumbnail image into our [ImageView].
     */
    private fun loadThumbnail() {
        Picasso.with(imageview_header!!.context)
                .load(mItem!!.thumbnailUrl)
                .noFade()
                .into(imageview_header)
    }

    /**
     * Load the item's full-size image into our [ImageView].
     * setPlaceholder = true
     */
    private fun loadFullSizeImage() {
        Picasso.with(this)
                .load(mItem!!.photoUrl)
                .noFade()
                //加载大图的时候 设置为noPlaceholder的话就不会有白屏现象发生
                //发现过程：本来设想通过把原imageView上面的缩略图设置为placeHolder的话
                //就能够无缝的衔接了  试过之后发现果然可以 然后设想那个空白是一个空白的placeHolder
                .noPlaceholder()//OK  this method can prevent the blank
//                .placeholder(null)
                .into(imageview_header)
    }

    /**
     * Try and add a [Transition.TransitionListener] to the entering shared element
     * [Transition]. We do this so that we can load the full-size image after the transition
     * has completed.

     * @return true if we were successful in adding a listener to the enter transition
     */
    private fun addTransitionListener(): Boolean {
        //min API 21--
        val transition = window.sharedElementEnterTransition

        if (transition != null) {
            // There is an entering shared element transition so add a listener to it
            transition.addListener(object : Transition.TransitionListener {
                override fun onTransitionEnd(transition: Transition) {
                    // As the transition has ended, we can now load the full-size image
                    //图片加载的空白间隙 是由于这个方法和上个一个方法的衔接问题造成的
                    loadFullSizeImage()

                    // Make sure we remove ourselves as a listener
                    transition.removeListener(this)
                }

                override fun onTransitionStart(transition: Transition) {
                    // No-op
                    //为了调查加载图片闪烁的问题：尝试把加载缩略图放到start的位置
                    //放到这里 G:不会影响动画也不会影响动画加载
                    //B：并没有解决闪烁的问题 依旧会有加载的空白发生
                    loadThumbnail()
                }

                override fun onTransitionCancel(transition: Transition) {
                    // Make sure we remove ourselves as a listener
                    transition.removeListener(this)
                }

                override fun onTransitionPause(transition: Transition) {
                    // No-op
                }

                override fun onTransitionResume(transition: Transition) {
                    // No-op
                }
            })
            return true
        }

        // If we reach here then we have not added a listener
        return false
    }

    companion object {

        // Extra name for the ID parameter
        val EXTRA_PARAM_ID = "detail:_id"

        // View name of the header image. Used for activity scene transitions
        val VIEW_NAME_HEADER_IMAGE = "detail:header:image"

        // View name of the header title. Used for activity scene transitions
        val VIEW_NAME_HEADER_TITLE = "detail:header:title"
    }

}
