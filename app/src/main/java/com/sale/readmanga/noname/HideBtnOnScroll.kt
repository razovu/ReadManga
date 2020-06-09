package com.sale.readmanga.noname

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewPropertyAnimator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.animation.AnimationUtils

/**
 * Тупанул и просто скопипастил класс FloatingActionButton. Изменил направление анимации
 */

open class HideBtnOnScroll<V : View?>(context: Context?, attrs: AttributeSet?) :
    CoordinatorLayout.Behavior<V>(context, attrs) {
    private var height = 0
    private var currentState = 2
    private var currentAnimator: ViewPropertyAnimator? = null


    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        height = child!!.measuredHeight
        return super.onLayoutChild(parent, child, layoutDirection)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        directTargetChild: View,
        target: View,
        nestedScrollAxes: Int): Boolean {
        return nestedScrollAxes == 2
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int) {
        if (currentState != 1 && dyConsumed > 0) {
            slideDown(child)
        } else if (currentState != 2 && dyConsumed < 0) {
            slideUp(child)
        }
    }

/** По сути достаточно просто наследоваться от HideBtnOnScrollBehavior
    и переопределить методы slideUp и slideDown.
    Но у меня чет не вышло сначала  */


    protected fun slideUp(child: V) {
        if (currentAnimator != null) {
            currentAnimator!!.cancel()
            child!!.clearAnimation()
        }
        currentState = 2
        animateChildTo(child, 0, 225L, AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR)
    }

    protected fun slideDown(child: V) {
        if (currentAnimator != null) {
            currentAnimator!!.cancel()
            child!!.clearAnimation()
        }
        currentState = 1
        animateChildTo(child, -150, 175L, AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR)
    }

    private fun animateChildTo(child: V, targetY: Int, duration: Long, interpolator: TimeInterpolator) {
        currentAnimator = child!!.animate().translationY(targetY.toFloat()).setInterpolator(interpolator)
                .setDuration(duration).setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        currentAnimator = null
                    }
                })
    }

//    companion object {
//        protected const val ENTER_ANIMATION_DURATION = 225
//        protected const val EXIT_ANIMATION_DURATION = 175
//        private const val STATE_SCROLLED_DOWN = 1
//        private const val STATE_SCROLLED_UP = 2
//    }
}


