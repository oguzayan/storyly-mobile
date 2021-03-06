package com.appsamurai.storyly.reactnative

import android.content.Context
import android.view.Choreographer
import android.widget.FrameLayout
import com.appsamurai.storyly.Story
import com.appsamurai.storyly.StorylyListener
import com.appsamurai.storyly.StorylyView
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.uimanager.events.RCTEventEmitter


class STStorylyView(context: Context) : FrameLayout(context) {
    internal var storylyView: StorylyView = StorylyView(context)

    init {
        addView(storylyView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        storylyView.storylyListener = object : StorylyListener {
            override fun storylyActionClicked(storylyView: StorylyView, story: Story): Boolean {
                sendEvent(STStorylyManager.EVENT_STORYLY_ACTION_CLICKED, Arguments.createMap().also { storyMap ->
                    storyMap.putInt("index", story.index)
                    storyMap.putString("title", story.title)
                    storyMap.putMap("media", Arguments.createMap().also { storyMediaMap ->
                        storyMediaMap.putInt("type", story.media.type.ordinal)
                        storyMediaMap.putString("url", story.media.url)
                        storyMediaMap.putString("buttonText", story.media.buttonText)
                        storyMediaMap.putString("actionUrl", story.media.actionUrl)
                        storyMediaMap.putMap("data", Arguments.createMap().also { storyDataMap ->
                            story.media.data?.forEach {
                                storyDataMap.putString(it.key, it.value)
                            }
                        })
                    })
                })
                return true
            }

            override fun storylyLoaded(storylyView: StorylyView) {
                manuallyLayout()
                sendEvent(STStorylyManager.EVENT_STORYLY_LOADED, null)
            }

            override fun storylyLoadFailed(storylyView: StorylyView) {
                sendEvent(STStorylyManager.EVENT_STORYLY_LOAD_FAILED, null)
            }
        }

        Choreographer.getInstance().postFrameCallback {
            manuallyLayout()
            viewTreeObserver.dispatchOnGlobalLayout()
        }
    }

    private fun manuallyLayout() {
        storylyView.measure(MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY))
        storylyView.layout(0, 0, storylyView.measuredWidth, storylyView.measuredHeight)
    }

    private fun sendEvent(eventName: String, eventParameters: WritableMap?) {
        (context as? ReactContext)?.getJSModule(RCTEventEmitter::class.java)?.receiveEvent(id, eventName, eventParameters)
    }
}
