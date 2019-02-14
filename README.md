# Android焦点

先通过一个demo来看看android原生的焦点和我们的产品需求有哪些不符的地方。

## 问题一：焦点查找的规律是怎么样的？

### 期望结果：

Recyclerview聚焦到最后一个Item，继续按下键，焦点保持不变。

### 实际结果

Recyclerview聚焦到最后一个Item，继续按下键，焦点会跳出RecyclerView，跳到附近的View上。

![1.1](/Users/wuhaoteng/Desktop/1.1.gif)

### 问题分析

那么当Recyclerview滑动到最底部时，按下键，Android系统是如何找到下一个需要被聚焦的view的呢？我们把断点打在ViewGroup的focusSearch方法上，可以看到从ViewRootImp的performFocusNavigation方法开始，依次调用了如下方法。

![1](/Users/wuhaoteng/Desktop/1.png)

#### View的focusSearch方法

View并不会直接去找焦点，而是交给它的parent去找。

```java
 public View focusSearch(@FocusRealDirection int direction) {
        if (mParent != null) {
            //直接交给viewgroup去查找焦点
            return mParent.focusSearch(this, direction);
        } else {
            return null;
        }
    }
```

#### ViewGroup的focusSearch方法

焦点会逐级的交给父ViewGroup的focusSearch方法去处理，直到最外层的布局，最后实际上是调用了FocusFinder的findNextFocus方法去寻找新的焦点。

```java
 public View focusSearch(View focused, int direction) {
        if (isRootNamespace()) {
            //如果不再viewgroup的focusSearch方法中做拦截，会一直到最顶层的DecorView
            return FocusFinder.getInstance().findNextFocus(this, focused, direction);
        } else if (mParent != null) {
            return mParent.focusSearch(focused, direction);
        }
        return null;
    }
```

但是这里要注意的是，RecyclerView和其他的ViewGroup不一样，它自己重写了focusSearch方法。所以在焦点查找委托到达到DecorView之前，会先执行RecyclerView的focusSearch方法。

那么，RecyclerView和其他ViewGroup在寻找焦点方面有什么不一样呢？**为什么RecyclerView要重写ViewGroup的焦点查找机制呢**？想知道这些问题的答案，那我们首先要知道ViewGroup的焦点查找机制。

#### FocusFinder的findNextFocus方法

ViewGroup的焦点查找机制的核心其实就是FocusFinder的findNextFocus方法。

主要步骤：

1. `findNextUserSpecifiedFocus`  优先从xml或者代码中**指定focusId**的View中找。
2. `addFocusables ` 将**可聚焦**且**可见**的view加入到集合中。
3. `findNextFocus`  在集合中找到**最近**的一个。

```java
 private View findNextFocus(ViewGroup root, View focused, Rect focusedRect, int direction) {
        View next = null;
        ViewGroup effectiveRoot = getEffectiveRoot(root, focused);
        if (focused != null) {
           //从自己开始向下遍历，如果没找到则从自己的parent开始向下遍历，直到找到id匹配的视图为止。
           //也许存在多个相同id的视图，这个方法只会返回在View树中节点范围最近的一个视图。
            next = findNextUserSpecifiedFocus(effectiveRoot, focused, direction);
        }
        if (next != null) {
            return next;
        }
        ArrayList<View> focusables = mTempList;
        try {
            focusables.clear();
            //找到root下所有isVisible && isFocusable的View 
            effectiveRoot.addFocusables(focusables, direction);
            if (!focusables.isEmpty()) {
                //从focusables中找到最近的一个
                next = findNextFocus(effectiveRoot, focused, focusedRect, direction, focusables);
            }
        } finally {
            focusables.clear();
        }
        return next;
    }
```

#### ViewGroup的addFocusables方法

主要注意三点:

1. **descendantFocusability属性决定了ViewGroup和其子view的聚焦优先级**

- FOCUS_BLOCK_DESCENDANTS：viewgroup会覆盖子类控件而直接获得焦点
- FOCUS_BEFORE_DESCENDANTS：viewgroup会覆盖子类控件而直接获得焦点
- FOCUS_AFTER_DESCENDANTS：viewgroup只有当其子类控件不需要获取焦点时才获取焦点

2. **addFocusables的第一个参数views是由root决定的。在ViewGroup的focusSearch方法中传进来的root是DecorView，当然我们也可以主动调用FocusFinder的findNextFocus方法，在指定的ViewGroup中查找焦点**。
3. **view 不仅要满足focusable的条件，还要满足visiable的条件**。这个条件决定了RecyclerView为什么要自己实现focusSearch，比如RecyclerView聚焦在按键方向上、当前屏幕区域内可见的最后一个item时（其实后面还有n个item），如果用ViewGroup的focusSearch方法，那么当前不可见的下一个item将无法获得焦点。这和我们正常所看到的现象 *“按下键，RecyclerView向上滚动，焦点聚焦到下一个item上”* 的这种现象不符。具体原因我们之后分析RecyclerView的focusSearch方法时再说。
```java
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        final int focusableCount = views.size();

        final int descendantFocusability = getDescendantFocusability();
        final boolean blockFocusForTouchscreen = shouldBlockFocusForTouchscreen();
        final boolean focusSelf = (isFocusableInTouchMode() || !blockFocusForTouchscreen);

        if (descendantFocusability == FOCUS_BLOCK_DESCENDANTS) {
            if (focusSelf) {
                //FOCUS_BLOCK_DESCENDANTS，这里只将viewgroup自身加入到focusable集合当中，所以之				 后的焦点查找只能找到ViewGroup自身而不能找到它的子view
                super.addFocusables(views, direction, focusableMode);
            }
            return;
        }

        if (blockFocusForTouchscreen) {
            focusableMode |= FOCUSABLES_TOUCH_MODE;
        }

        if ((descendantFocusability == FOCUS_BEFORE_DESCENDANTS) && focusSelf) {
            //FOCUS_BEFORE_DESCENDANTS，先将ViewGroup加入到focusable集合中
            super.addFocusables(views, direction, focusableMode);
        }

        //之后再将子View加入到focusable集合中
        int count = 0;
        final View[] children = new View[mChildrenCount];
        for (int i = 0; i < mChildrenCount; ++i) {
            View child = mChildren[i];
            if ((child.mViewFlags & VISIBILITY_MASK) == VISIBLE) {
                //view 不仅要满足focusable的条件，还要满足visiable的条件
                children[count++] = child;
            }
        }
     
        FocusFinder.sort(children, 0, count, this, isLayoutRtl());
        for (int i = 0; i < count; ++i) {
            children[i].addFocusables(views, direction, focusableMode);
        }
        
        if ((descendantFocusability == FOCUS_AFTER_DESCENDANTS) && focusSelf
                && focusableCount == views.size()) {
            //FOCUS_AFTER_DESCENDANTS,只有当ViewGroup没有focusable的子View时，才会把ViewGroup			 自身加入到focusable集合中，否则集合中只有ViewGroup的子View
            super.addFocusables(views, direction, focusableMode);
        }
    }

```

#### FocusFInder的findNextFocus方法

在addFocusables之后，找到指定方向上与当前focused距离最近的view。在进行查找之前，会统一坐标系。

```java
  private View findNextFocus(ViewGroup root, View focused, Rect focusedRect,
            int direction, ArrayList<View> focusables) {
        if (focused != null) {
            if (focusedRect == null) {
                focusedRect = mFocusedRect;
            }
            //取得考虑scroll之后的焦点Rect，该Rect是相对focused视图本身的
            focused.getFocusedRect(focusedRect);
            //将当前focused视图的坐标系，转换到root的坐标系中，统一坐标，以便进行下一步的计算
            root.offsetDescendantRectToMyCoords(focused, focusedRect);
        } else {
          ...
        }

        switch (direction) {
            ...
            case View.FOCUS_UP:
            case View.FOCUS_DOWN:
            case View.FOCUS_LEFT:
            case View.FOCUS_RIGHT:
                //统一坐标系后，进入比较核心的焦点查找逻辑
                return findNextFocusInAbsoluteDirection(focusables, root, focused,
                        focusedRect, direction);
            default:
                throw new IllegalArgumentException("Unknown direction: " + direction);
        }
    }
```

#### FocusFInder的findNextFocusInAbsoluteDirection方法

总的来说就是根据当前focused的位置以及按键的方向，循环比较focusable集合中哪一个最适合，然后返回最合适的view，焦点查找就算完成了。

```java
 protected View findNextFocusInAbsoluteDirection(ArrayList<View> focusables, ViewGroup root, View focused,Rect focusedRect, int direction) {
       //先在当前focused的位置上虚构出一个候选Rect
        mBestCandidateRect.set(focusedRect);
        switch(direction) {
            ...
            case View.FOCUS_DOWN:
                //把focusedRect向上移一个"身位"，按键向下，那么他肯定就是优先级最低的了
                mBestCandidateRect.offset(0, -(focusedRect.height() + 1));
        }

        View closest = null;

        int numFocusables = focusables.size();
     	//遍历root下所有可聚焦的view
        for (int i = 0; i < numFocusables; i++) {
            View focusable = focusables.get(i);
			//如果focusable是当前focused或者root，跳过继续找
            if (focusable == focused || focusable == root) continue;
            
            //将当前focusable也进行统一坐标
            focusable.getFocusedRect(mOtherRect);
            root.offsetDescendantRectToMyCoords(focusable, mOtherRect);
            
			//进行比较
            if (isBetterCandidate(direction, focusedRect, mOtherRect, mBestCandidateRect)) {
                //如果focusable通过筛选条件，赋值给mBestCandidateRect，继续循环比对
                mBestCandidateRect.set(mOtherRect);
                closest = focusable;
            }
        }
        return closest;
    }
```

#### FocusFinder的isBetterCandidate方法

用于比较的方法。分别是将**当前聚焦的view**，**当前遍历到的focusable**和**目前为止最合适的focusable**（i = 0时是优先级最低的rect）进行比较。

```java
/**
  *@param source 当前focused
  *@param rect1  当前focusable
  *@param rect2  目前为止最合适的focusable
  */   
boolean isBetterCandidate(int direction, Rect source, Rect rect1, Rect rect2) {
        // to be a better candidate, need to at least be a candidate in the first
        // place :)
        if (!isCandidate(source, rect1, direction)) {
            return false;
        }
		
        // we know that rect1 is a candidate.. if rect2 is not a candidate,
        // rect1 is better
        if (!isCandidate(source, rect2, direction)) {
            return true;
        }

        // if rect1 is better by beam, it wins
        if (beamBeats(direction, source, rect1, rect2)) {
            return true;
        }

        // if rect2 is better, then rect1 cant' be :)
        if (beamBeats(direction, source, rect2, rect1)) {
            return false;
        }

        // otherwise, do fudge-tastic comparison of the major and minor axis
        return (getWeightedDistanceFor(
                        majorAxisDistance(direction, source, rect1),
                        minorAxisDistance(direction, source, rect1))
                < getWeightedDistanceFor(
                        majorAxisDistance(direction, source, rect2),
                        minorAxisDistance(direction, source, rect2)));
    }
```

#### FocusFinder的isCandidate方法

判断是否可以做为候选。可以看作是一个初步筛选的方法，但是到底哪个更好还需要看beamBeat方法，这个方法会将通过筛选的focusable和当前最合适的focusable进行比较，选出更合适的一个。

```java
boolean isCandidate(Rect srcRect, Rect destRect, int direction) {
        switch (direction) {
           ...
            //这里就拿按下键举例，别的方向同理
            case View.FOCUS_DOWN:
                //这个判断画个图就很好理解了（见下图）
                return (srcRect.top < destRect.top || srcRect.bottom <= destRect.top)
                        && srcRect.bottom < destRect.bottom;
        }
        throw new IllegalArgumentException("direction must be one of "
                + "{FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT}.");
    }
```

到这里为止ViewGroup的focusSearch方法基本上就讲完了。那么下面来看一下RecyclerView的focusSearch方法是如何实现焦点查找的。

#### RecyclerView的FocusSearch方法

前面讲到了，该方法主要是为了解决**RecyclerView聚焦在按键方向上、当前屏幕区域内可见的最后一个item时，当前不可见的下一个item将无法获得焦点。**

```java
 public View focusSearch(View focused, int direction) {
     	//可以在LayoutManager.onInterceptFocusSearch()中做一些焦点拦截操作
        View result = mLayout.onInterceptFocusSearch(focused, direction);
        if (result != null) {
            return result;
        }
        final boolean canRunFocusFailure = mAdapter != null && mLayout != null
                && !isComputingLayout() && !mLayoutFrozen;

        final FocusFinder ff = FocusFinder.getInstance();
        if (canRunFocusFailure
            && (direction == View.FOCUS_FORWARD || direction == View.FOCUS_BACKWARD)) {
            ....
        } else {
            result = ff.findNextFocus(this, focused, direction);
            if (result == null && canRunFocusFailure) {
                 //result == null，说明在当前recyclerview中，当前聚焦的位置，当前按键方向上，当前屏				 幕区域内，找不到下一个可以聚焦的点了。
                consumePendingUpdateOperations();
                final View focusedItemView = findContainingItemView(focused);
                if (focusedItemView == null) {
                    return null;
                }
                startInterceptRequestLayout();
				//焦点搜索失败处理
                result = mLayout.onFocusSearchFailed(focused, direction, mRecycler, mState);
                stopInterceptRequestLayout(false);
            }
        }
        if (result != null && !result.hasFocusable()) {
            if (getFocusedChild() == null) {
                return super.focusSearch(focused, direction);
            }
            requestChildOnScreen(result, null);
            return focused;
        }
     
     	//判断result是否合适，如果不合适，调用ViewGroup的focusSearch方法
     	//这个方法和FocusFinder的isCandidate方法实现几乎一样
        return isPreferredNextFocus(focused, result, direction)
                ? result : super.focusSearch(focused, direction);
    }
```

#### mLayout的onFocusSearchFailed方法

这个方法是由LayoutManager来实现的，这就是RecyclerView的针对上面提到的情况的焦点查找方法。这里主要分析LinearLayoutManager中实现的该方法，如果在使用其他的LayoutManager时出现RecyclelerView焦点不符合预期的话，可以查看对于LayoutManager下的onFocusSearchFailed方法。

主要关注findPartiallyOrCompletelyInvisibleChildClosestToEnd方法，通过这个方法的命名我们大致就可以看出来这个方法的作用了。这个方法主要会**根据当前RecyclerVIew的正逆序以及按键方向，找出最近一个部分或完全不可见的View**。

```java
 public View onFocusSearchFailed(View focused, int focusDirection,
            RecyclerView.Recycler recycler, RecyclerView.State state) {
        resolveShouldLayoutReverse();
        if (getChildCount() == 0) {
            return null;
        }

        final int layoutDir = convertFocusDirectionToLayoutDirection(focusDirection);
        if (layoutDir == LayoutState.INVALID_LAYOUT) {
            return null;
        }
        ensureLayoutState();
        ensureLayoutState();
        final int maxScroll = (int) (MAX_SCROLL_FACTOR * mOrientationHelper.getTotalSpace());
        updateLayoutState(layoutDir, maxScroll, false, state);
        mLayoutState.mScrollingOffset = LayoutState.SCROLLING_OFFSET_NaN;
        mLayoutState.mRecycle = false;
        fill(recycler, mLayoutState, state, true);

        // nextCandidate is the first child view in the layout direction that's partially
        // within RV's bounds, i.e. part of it is visible or it's completely invisible but still
        // touching RV's bounds. This will be the unfocusable candidate view to become visible onto
        // the screen if no focusable views are found in the given layout direction.
        final View nextCandidate;
        if (layoutDir == LayoutState.LAYOUT_START) {
            nextCandidate = findPartiallyOrCompletelyInvisibleChildClosestToStart(recycler, state);
        } else {
            //获取距离底部最近的部分或者整体不可见的item，当RecyclerView滑到最底部是会返回null
            nextCandidate = findPartiallyOrCompletelyInvisibleChildClosestToEnd(recycler, state);
        }
        // nextFocus is meaningful only if it refers to a focusable child, in which case it
        // indicates the next view to gain focus.
        final View nextFocus;
        if (layoutDir == LayoutState.LAYOUT_START) {
            nextFocus = getChildClosestToStart();
        } else {
            nextFocus = getChildClosestToEnd();
        }
        if (nextFocus.hasFocusable()) {
            if (nextCandidate == null) {
                return null;
            }
            return nextFocus;
        }
        return nextCandidate;
    }
```

#### RecyclerView的isPreferredNextFocus方法

这个方法是RecyclerView内部的方法，和FocusFinder中的isCandidate方法的逻辑可以说几乎是一摸一样的。

- return false：说明最终会执行ViewGroup的FocusSearch方法去寻找焦点，这就出现了一开始demo中焦点跳出RecyclerView的现象。
- return true：说明焦点查找已经完成，next就是将要被聚焦的点。

```java
    private boolean isPreferredNextFocus(View focused, View next, int direction) {
        if (next == null || next == this) {
            //这里就是RecyclerView聚焦在最后一个item，继续按下键，这里会return false
            return false;
        }
        
        if (findContainingItemView(next) == null) {
            return false;
        }
        if (focused == null) {
            return true;
        }

        if (findContainingItemView(focused) == null) {
            return true;
        }

        //下面的逻辑和FocusFinder的isCandidate方法一摸一样，只是RecyclerView内部自己又实现了一遍
        mTempRect.set(0, 0, focused.getWidth(), focused.getHeight());
        mTempRect2.set(0, 0, next.getWidth(), next.getHeight());
        offsetDescendantRectToMyCoords(focused, mTempRect);
        offsetDescendantRectToMyCoords(next, mTempRect2);
        final int rtl = mLayout.getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL ? -1 : 1;
        int rightness = 0;
        if ((mTempRect.left < mTempRect2.left
                || mTempRect.right <= mTempRect2.left)
                && mTempRect.right < mTempRect2.right) {
            rightness = 1;
        } else if ((mTempRect.right > mTempRect2.right
                || mTempRect.left >= mTempRect2.right)
                && mTempRect.left > mTempRect2.left) {
            rightness = -1;
        }
        int downness = 0;
        if ((mTempRect.top < mTempRect2.top
                || mTempRect.bottom <= mTempRect2.top)
                && mTempRect.bottom < mTempRect2.bottom) {
            downness = 1;
        } else if ((mTempRect.bottom > mTempRect2.bottom
                || mTempRect.top >= mTempRect2.bottom)
                && mTempRect.top > mTempRect2.top) {
            downness = -1;
        }
        switch (direction) {
            case View.FOCUS_LEFT:
                return rightness < 0;
            case View.FOCUS_RIGHT:
                return rightness > 0;
            case View.FOCUS_UP:
                return downness < 0;
            case View.FOCUS_DOWN:
                return downness > 0;
            case View.FOCUS_FORWARD:
                return downness > 0 || (downness == 0 && rightness * rtl >= 0);
            case View.FOCUS_BACKWARD:
                return downness < 0 || (downness == 0 && rightness * rtl <= 0);
        }
        throw new IllegalArgumentException("Invalid direction: " + direction + exceptionLabel());
    }
```

到此为止ViewGroup的focusSearch和RecyclerVIew的focusSearch都分析完了。我们已经知道RecyclerView滑动到最底部的时候，发生了哪些焦点行为，那么解决起来就比较简单了。

#### focusSearch小结

结合KeyEvent事件的流转，处理焦点的时机，按照优先级（顺序）依次是：

1. dispatchKeyEvent
2. mOnKeyListener.onKey
3. onKeyDown/onKeyUp
4. focusSearch
5. 指定nextFocusId
6. 系统自动从所有isFocusable的视图中找下一个焦点视图，所以某些时候也可以在addFocusables方法中进行一些处理来改变焦点

以上任一处都可以指定焦点，一旦消费了就不再往下走。

比如前面说到了RecyclerView就是通过重写focusSearch方法对边界上部分可见或不可见的view的焦点查找进行了特殊处理。

### 解决方案

重写RecyclerView的focusSearch方法

```java
    public View focusSearch(View focused, int direction) {
        //通过super.focusSearch找到的view
        View realNextFocus = super.focusSearch(focused, direction);
        //RecyclerView内部下一个可聚焦的点
        View nextFocus = FocusFinder.getInstance().findNextFocus(this, focused, direction);
        switch (direction) {
            case FOCUS_RIGHT:
           		...
                break;
            case FOCUS_LEFT:
                ...
                break;
            case FOCUS_UP:
                ...
                break;
            case FOCUS_DOWN:
                //canScrollVertically(1)  true表示能滚动，false表示已经滚动到底部
        		//canScrollVertically(-1) true表示能滚动，false表示已经滚动到顶部
                if (nextFocus == null && !canScrollVertically(1)) {
                    //如果RecyclerView内部不存在下一个可聚焦的view，屏蔽焦点移动
                    return null;
                }
                break;
        }
        return realNextFocus;
```

## 问题二：如何来监听ViewGroup子View的聚焦和失焦状态

### 期望结果

只要ViewGroup的内部或自身存在焦点，ViewGroup就始终保持聚焦样式。

![2.2](/Users/wuhaoteng/Desktop/2.2.gif)

### 实际结果

在不做任何处理的情况下，一个页面只会存在一个聚焦的view。

![2.1](/Users/wuhaoteng/Desktop/2.1.gif)

### 问题分析

如果我们先不考虑完全重写Android焦点框架的情况，我们能否做一些特殊处理，来实现我们期望的结果呢？从期望结果描述来看，其实实现逻辑还是比较清晰的，就是我们需要拿到两个回调：

1. 当ViewGroup自身或者内部的View获得焦点的回调。
2. 当ViewGroup自身或者内部的View失去焦点的回调。

这就需要我们来看一下View和ViewGroup在requestFocus的过程中触发了哪些回调。

#### View的requestFocus方法

```java
public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        return requestFocusNoSearch(direction, previouslyFocusedRect);
    }
```

#### View的requestFocusNoSearch方法

requestFocusNoSearch校验View的属性，获取焦点的前提条件是“可见的”和“可聚焦的”。

```java
 private boolean requestFocusNoSearch(int direction, Rect previouslyFocusedRect) {
        // focusable且visible
        if ((mViewFlags & FOCUSABLE) != FOCUSABLE
                || (mViewFlags & VISIBILITY_MASK) != VISIBLE) {
            return false;
        }

        // 如果是触摸屏，需要focusableInTouchMode属性为true
        if (isInTouchMode() &&
            (FOCUSABLE_IN_TOUCH_MODE != (mViewFlags & FOCUSABLE_IN_TOUCH_MODE))) {
               return false;
        }

        // 判断parent viewGroup是否设置了FOCUS_BLOCK_DESCENDANTS
        if (hasAncestorThatBlocksDescendantFocus()) {
            return false;
        }

     	//实现View获取焦点的具体逻辑
        handleFocusGainInternal(direction, previouslyFocusedRect);
        return true;
    }
```

#### View的handleFocusGainInternal方法

这个是最核心的聚焦逻辑

```java
 void handleFocusGainInternal(@FocusRealDirection int direction, Rect previouslyFocusedRect) {
        if (DBG) {
            System.out.println(this + " requestFocus()");
        }

        if ((mPrivateFlags & PFLAG_FOCUSED) == 0) {
            //当前view没有被聚焦才会进入下面的逻辑
            //将view的聚焦标识设置为已聚焦
            mPrivateFlags |= PFLAG_FOCUSED;

            View oldFocus = (mAttachInfo != null) ? getRootView().findFocus() : null;

            if (mParent != null) {
                //通知父控件即将获取焦点
                mParent.requestChildFocus(this, this);
                updateFocusedInCluster(oldFocus, direction);
            }

            if (mAttachInfo != null) {
                //触发全局OnGlobalFocusChangeListener的回调
                mAttachInfo.mTreeObserver.dispatchOnGlobalFocusChange(oldFocus, this);
            }

            //触发将要被聚焦的View的OnFocusChangeListener回调
            onFocusChanged(true, direction, previouslyFocusedRect);
            //系统焦点样式变化，比如我们在Drawable中设置了focused_state来区别聚焦或未聚焦样式
            refreshDrawableState();
        }
    }
```

#### ViewGroup的requestChildFocus方法

```java
   public void requestChildFocus(View child, View focused) {
        if (DBG) {
            System.out.println(this + " requestChildFocus()");
        }
        if (getDescendantFocusability() == FOCUS_BLOCK_DESCENDANTS) {
            return;
        }

       //被聚焦的ViewGroup先会调用一下View的unFocus方法
        super.unFocus(focused);

       	
        if (mFocused != child) {
            if (mFocused != null) {
              	//mFocused就是当前ViewGroup下持有焦点的View或者ViewGroup，是串联整个焦点路径的属性
                //注意：View的unFocu方法和ViewGroup的unFocus方法实现是不一样的
                mFocused.unFocus(focused);
            }
			//把当前最新的焦点child赋值给mFocused
            mFocused = child;
        }
        if (mParent != null) {
            //继续往上通知parent
            mParent.requestChildFocus(this, focused);
        }
    }
```

**View的unFocus方法和ViewGroup的unFocus方法实现是不一样的**，这里如果没有看清楚可能就会对焦点事件的回调的方法出现一些误会。

#### ViewGroup的unFocus方法

这个方法实际上不是失焦的逻辑，而是一个递归调用，最终会执行View的unFocus方法。View的unFocus方法才是真正的失焦逻辑。

```java
   void unFocus(View focused) {
        if (DBG) {
            System.out.println(this + " unFocus()");
        }
        if (mFocused == null) {
            super.unFocus(focused);
        } else {
            //递归调用，最终会执行当前聚集的View的unFocus方法
            mFocused.unFocus(focused);
            mFocused = null;
        }
    }

```

#### View的unFocus方法

有两个地方会调用到这个方法：

1. 在ViewGroup的unFocus方法中递归调用，最终执行当前聚焦的view的unfocus方法。
2. 在ViewGroup中调用super.unFocus()。这个是在requestChildFocus方法中进行调用的，用于在子View聚焦之前，先清除一下自身的焦点。

总的来说就是两种情况，**当前聚焦的View失去焦点**和**下一个要被聚焦的View的ViewGroup清除自身焦点**。也就是说：

> 对于View来说，每次聚焦或者失焦都会触发View的unFocus方法。
>
> 对于ViewGroup来说，当焦点从ViewGroup外进入到ViewGroup内的子View上时，会触发View的unFocus方法。而ViewGroup内的子View失去焦点时，不会触发View的unFocus方法。

这就直接关系到ViewGroup的onFocusChanged方法是否执行，具体逻辑看View的clearFocusInternal方法。
```java
  void unFocus(View focused) {
        if (DBG) {
            System.out.println(this + " unFocus()");
        }

        clearFocusInternal(focused, false, false);
    }
```

#### View的clearFocusInternal方法

clearFocusInternal方法还被clearFocus方法所调用，注意区别。clearFocus方法是通过用户**主动调用**而失去焦点，而unFocus方法是在新的焦点要被聚焦之前，系统内部调用的。

```java
  void clearFocusInternal(View focused, boolean propagate, boolean refocus) {
        if ((mPrivateFlags & PFLAG_FOCUSED) != 0) {
            //view存在焦点才会执行这里面的逻辑
            //将view的聚焦标识设置为未聚焦
            mPrivateFlags &= ~PFLAG_FOCUSED;

            if (propagate && mParent != null) {
                //只有主动调用clearfocus方法时才会执行
                mParent.clearChildFocus(this);
            }
			//onFocusChanged回调
            onFocusChanged(false, 0, null);
            //系统的焦点样式变化
            refreshDrawableState();

            if (propagate && (!refocus || !rootViewRequestFocus())) {
                //只有主动调用clearfocus方法时才会执行全局焦点变化监听的方法
                //这是由于在unFocus之后，handleFocusGainInternal方法中会继续执行全局焦点变化监          					听，这里没必要重复执行。
                notifyGlobalFocusCleared(this);
            }
        }
    }
```

#### View的clearFocus方法

```java
 public void clearFocus() {
        if (DBG) {
            System.out.println(this + " clearFocus()");
        }

        clearFocusInternal(null, true, true);
    }
```

#### requestFocus小结

将要失焦的View：focused   

将要失焦的View上层的所有ViewGroup：focusedParent

将要被聚焦的View：next     

将要被聚焦的View上层的所有ViewGroup：nextParent

一次聚焦事件回调方法执行的顺序是这样的：

1. nextParent.requestChildFocus(focused , focused) ;

2. nextParent.onFocusChanged(false, 0, null);

3. focused.onFocusChanged(false, 0, null) ;

4. mTreeObserver.dispatchOnGlobalFocusChange(focused , next);

5. next.onFocusChanged(true, direction, previouslyFocusedRect)

如果我们主动调用了clearFocus方法来失去焦点，那么回调方法的执行顺序是这样的：

1. mParent.clearChildFocus(focused);

2. focused.onFocusChanged(false, 0, null);
3. mAttachInfo.mTreeObserver.dispatchOnGlobalFocusChange(focused , null);

聚焦流程基本分析完了，回到我们的问题，我们需要监听ViewGroup内的View的焦点变化。子View获取焦点我们可以通过requestChildFocus方法，但是并没有子View失去焦点的监听（除非我们主动调用clearFocus方法）。

或许我们只能通过**ViewTreeObserve的dispatchOnGlobalFocusChange**方法方法来监听这个变化。

#### ViewTreeObserve

使用方法，在ViewGroup中注册：

```java
 getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                if (hasFocus()) {
                    //焦点进入ViewGroup
                } else {
                    //焦点移出ViewGroup
                }
            }
        });
```

addOnGlobalFocusChangeListener方法

```java
    public void addOnGlobalFocusChangeListener(OnGlobalFocusChangeListener listener) {
        checkIsAlive();

        if (mOnGlobalFocusListeners == null) {
            mOnGlobalFocusListeners = new CopyOnWriteArrayList<OnGlobalFocusChangeListener>();
        }

        mOnGlobalFocusListeners.add(listener);
    }
```

dispatchOnGlobalFocusChange方法

```java
 final void dispatchOnGlobalFocusChange(View oldFocus, View newFocus) {
        final CopyOnWriteArrayList<OnGlobalFocusChangeListener> listeners = mOnGlobalFocusListeners;
        if (listeners != null && listeners.size() > 0) {
            for (OnGlobalFocusChangeListener listener : listeners) {
                listener.onGlobalFocusChanged(oldFocus, newFocus);
            }
        }
    }
```

这里的mOnGlobalFocusListeners是一个ArrayList，所以可以监听多个view的焦点变化。但是在使用的时候需要注意一个问题，注册的listener在不使用的时候要及时的remove，不然会非常影响性能。

### 解决方案

这里提供大致的思路，具体的方案可以看我写的demo。demo中还提供了聚焦后的焦点框以及放大的动画效果。

新建一个类继承自ViewGroup的子类（我这里继承了FrameLayout），分别在onAttachedToWindow方法中进行注册，在onDetachedFromWindow方法中进行解绑。

```java
@Override
protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    onGlobalFocusChangeListener = new ViewTreeObserver.OnGlobalFocusChangeListener() {
        @Override
        public void onGlobalFocusChanged(View oldFocus, View newFocus) {
            //判断是否自身被聚焦或者存在子view被聚焦
            if (hasFocus()) {
                focusEnter();
            } else {
                focusLeave();
            }
        }
    };
    getViewTreeObserver().addOnGlobalFocusChangeListener(onGlobalFocusChangeListener);
}

@Override
protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    //主要要及时remove
    getViewTreeObserver().removeOnGlobalFocusChangeListener(onGlobalFocusChangeListener);
}
```

使用这种方式，mOnGlobalFocusListeners的size等于RecyclerVIew中当前可见的继承于该ViewGroup的item的个数。

## 问题三：RecyclerView的焦点记忆

#### 期望结果

RecyclerView能够对焦点路径记忆。

![3.1](/Users/wuhaoteng/Desktop/3.1.gif)

#### 问题分析

也就是说，当焦点从RecyclerView上移出的时候，需要保存RecyclerView当前聚焦的View，下次RecyclerVIew将要获得焦点的时候，主动聚焦到上一次聚焦的View上。

#### 解决方案

1. 设置RecyclerView在优先于它的子View获取焦点

```java
setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
this.setFocusable(true);
```

2. 在requestChildFocus方法中记录被聚焦的view

```java
@Override
public void requestChildFocus(View child, View focused) {
       super.requestChildFocus(child, focused);
    if (null != child) {
        //取得获得焦点的item的position
        mLastFocusPosition = getChildViewHolder(child).getAdapterPosition();
    }
}
```

3. 在RecyclerView将要获取焦点的时候，主动设置上次一聚焦的View去获取焦点

  ```java
   @Override
   public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
     View lastFocusedView = getLayoutManager().findViewByPosition(mLastFocusPosition);
     lastFocusedView.requestFocus();
     return false;
   }
  ```


## 问题四：焦点变化时，Recyclerview是如何进行滚动的

#### 期望结果

Recyclerview滚动时，聚焦的item位置保持在中间。

![4.2](/Users/wuhaoteng/Desktop/4.2.gif)

#### 实际结果

![4.1](/Users/wuhaoteng/Desktop/4.1.gif)

#### 问题分析

需要在计算RecyclerView滑动距离的方法中进行重写，控制每次滑动的距离。先来看看RecyclerView原生的滑动距离计算方法。

#### RecyclerView的requestChildRectangleOnScreen方法

当RecyclerView的某个子View需要被定位在屏幕的某个矩形范围时，调用此方法。

```java
    /**
     * 通过该方法设置选中的item居中
     * <p>
     * 最终计算出的dy，dx的实际意义就是在滚动中上下和左右滑动的距离
     *
     * @param child     发出请求的子View
     * @param rect      子View坐标系内的矩形，即此子View希望在屏幕上的定位
     * @param immediate 设为true，则禁止动画和平滑移动滚动条
     * @return 进行了滚动操作的这个ViewGroup，是否处理此操作
     */
public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect,
        boolean immediate) {
    final int parentLeft = getPaddingLeft();
    final int parentTop = getPaddingTop();
    final int parentRight = getWidth() - getPaddingRight();
    final int parentBottom = getHeight() - getPaddingBottom();
    final int childLeft = child.getLeft() + rect.left - child.getScrollX();
    final int childTop = child.getTop() + rect.top - child.getScrollY();
    final int childRight = childLeft + rect.width();
    final int childBottom = childTop + rect.height();

    final int offScreenLeft = Math.min(0, childLeft - parentLeft);
    final int offScreenTop = Math.min(0, childTop - parentTop);
    final int offScreenRight = Math.max(0, childRight - parentRight);
    final int offScreenBottom = Math.max(0, childBottom - parentBottom);

    // Favor the "start" layout direction over the end when bringing one side or the other
    // of a large rect into view. If we decide to bring in end because start is already
    // visible, limit the scroll such that start won't go out of bounds.
    final int dx;
    if (getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
        dx = offScreenRight != 0 ? offScreenRight
                : Math.max(offScreenLeft, childRight - parentRight);
    } else {
        dx = offScreenLeft != 0 ? offScreenLeft
                : Math.min(childLeft - parentLeft, offScreenRight);
    }

    // Favor bringing the top into view over the bottom. If top is already visible and
    // we should scroll to make bottom visible, make sure top does not go out of bounds.
    final int dy = offScreenTop != 0 ? offScreenTop
            : Math.min(childTop - parentTop, offScreenBottom);

    if (dx != 0 || dy != 0) {
        if (immediate) {
            parent.scrollBy(dx, dy);
        } else {
            parent.smoothScrollBy(dx, dy);
        }
        return true;
    }
    return false;
}
```

#### 解决方案

```java
    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        //计算偏移量
        int selectedItemOffsetStart = 0;
        int selectedItemOffsetEnd = 0;
        selectedItemOffsetStart = !isVertical() ? (getFreeWidth() - child.getWidth()) : (getFreeHeight() - child.getHeight());
        selectedItemOffsetStart /= 2;
        selectedItemOffsetEnd = selectedItemOffsetStart;
        
        final int parentLeft = getPaddingLeft();
        final int parentTop = getPaddingTop();
        final int parentRight = getWidth() - getPaddingRight();
        final int parentBottom = getHeight() - getPaddingBottom();
        final int childLeft = child.getLeft() + rect.left - child.getScrollX();
        final int childTop = child.getTop() + rect.top - child.getScrollY();
        final int childRight = childLeft + rect.width();
        final int childBottom = childTop + rect.height();


        final int offScreenLeft = Math.min(0, childLeft - parentLeft - mSelectedItemOffsetStart);
        final int offScreenRight = Math.max(0, childRight - parentRight + mSelectedItemOffsetEnd);

        final int offScreenTop = Math.min(0, childTop - parentTop - mSelectedItemOffsetStart);
        final int offScreenBottom = Math.max(0, childBottom - parentBottom + mSelectedItemOffsetEnd);

        // Favor the "start" layout direction over the end when bringing one side or the other
        // of a large rect into view. If we decide to bring in end because start is already
        // visible, limit the scroll such that start won't go out of bounds.
        final int dx;
        if (getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            dx = offScreenRight != 0 ? offScreenRight
                    : Math.max(offScreenLeft, childRight - parentRight);
        } else {
            dx = offScreenLeft != 0 ? offScreenLeft
                    : Math.min(childLeft - parentLeft, offScreenRight);
        }

        // Favor bringing the top into view over the bottom. If top is already visible and
        // we should scroll to make bottom visible, make sure top does not go out of bounds.
        final int dy = offScreenTop != 0 ? offScreenTop
                : Math.min(childTop - parentTop, offScreenBottom);

        if (dx != 0 || dy != 0) {
            if (immediate) {
                scrollBy(dx, dy);
            } else {
                smoothScrollBy(dx, dy);
            }
            return true;
        }
        return false;
    }
```

这里要注意的是，为了适配v7，需要自定义LayoutManager，不然RecyclerView的requestChildRectangleOnScreen可能无法执行。

```java
public class V7LinearLayoutManager extends LinearLayoutManager {
    public V7LinearLayoutManager(Context context) {
        super(context);
    }

    public V7LinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public V7LinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
    }

    @Override
    public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate, boolean focusedChildVisible) {
        if(parent instanceof TvRecyclerView) {
            return parent.requestChildRectangleOnScreen(child, rect, immediate);
        }
        return super.requestChildRectangleOnScreen(parent, child, rect, immediate, focusedChildVisible);
    }
}
```

## 总结

1. 想要改变焦点查找规则，可以关注focusSearch的过程。

2. 想要监听焦点变化的回调，可以关注requestFocus的过程。

如果想要实现一套通用焦点框架，个人想法是在Android原生焦点机制的基础上做一些定制化的操作，或许并不需要完全自己去实现一套焦点框架。

TV端焦点问题的比较复杂的根本问题我认为有两点：

1. 主观因素可能是我们对Android原生的焦点机制还没有特别的清楚，所以不知道如何下手去处理一些不符合预期的现象。其实这些现象如果跟着源码去看的话，会发现它的实现都是有一定道理的。

2. 客观因素是某些的UI交互比较复杂，Andorid原生的焦点机制只是采用了比较折中的处理方案。没有什么语言是完美的，也没有什么框架是完美的，能满足我们需求才是最好的。所以我认为焦点问题的处理应该建立在我们有一套统一的UI交互的基础上，然后我们在去基于Android原生焦点机制做一些定制化的操作，具体如何定制化，基本上问题都可以在文中提到的几个回调接口中去处理。


大家也可以多想想有什么情景通过上面的方法是无法处理的，欢迎补充！
