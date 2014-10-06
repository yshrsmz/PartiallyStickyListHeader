package net.yslibrary.android.partiallystickylistheader.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.yslibrary.android.partiallystickylistheader.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;

public class PagerSlidingTabStrip extends HorizontalScrollView implements OnGlobalLayoutListener {

    @Override
    public void onGlobalLayout() {

    }

    public static interface OnTabChangeListener {
        public void onTabChanged(int tabPosition);
    }

    public interface IconTabProvider {
        public int getPageIconResId(int position);
    }

    // @formatter:off
    private static final int[] ATTRS = new int[] {
            android.R.attr.textSize,
            android.R.attr.textColor
    };
    // @formatter:on

    private int mCurrentPosition = 0;
    private float mCurrentPositionOffset = 0f;

    private LinearLayout.LayoutParams mDefaultTabLayoutParams;

    private int mDividerColor = 0x1A000000;
    private int mDividerPadding = 12;
    private Paint mDividerPaint;
    private int mDividerWidth = 1;

    private LinearLayout.LayoutParams mExpandedTabLayoutParams;

    private int mIndicatorColor = 0xFF666666;
    private int mIndicatorHeight = 8;
    private int mLastScrollX = 0;

    private Locale mLocale;

    private boolean mDraweUnderline = true;
    private OnTabChangeListener mOnTabChangeListener;

    private List<TabSpec> mTabs = new ArrayList<TabSpec>();

    private Paint mRectPaint;

    private boolean mShouldExpand = false;

    private int mTabBackgroundResId = R.drawable.background_tab;
    private int mTabCount;
    private int mTabPadding = 24;
    private int mTabTextSize = 12;
    private int mTabTextColor = 0xFF666666;
    private Typeface mTabTypeface = null;
    private int mTabTypefaceStyle = Typeface.BOLD;

    private LinearLayout mTabsContainer;
    private boolean mTextAllCaps = true;
    private int mUnderlineColor = 0x1A000000;
    private int mUnderlineHeight = 2;

    public PagerSlidingTabStrip(Context context) {
        this(context, null);
    }

    public PagerSlidingTabStrip(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerSlidingTabStrip(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setFillViewport(true);
        setWillNotDraw(false);

        mTabsContainer = new LinearLayout(context);
        mTabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        mTabsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mTabsContainer);

        DisplayMetrics dm = getResources().getDisplayMetrics();

        mIndicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mIndicatorHeight, dm);
        mUnderlineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mUnderlineHeight, dm);
        mDividerPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mDividerPadding, dm);
        mTabPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mTabPadding, dm);
        mDividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mDividerWidth, dm);
        mTabTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTabTextSize, dm);

        // get system attrs (android:textSize and android:textColor)

        TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

        mTabTextSize = a.getDimensionPixelSize(0, mTabTextSize);
        mTabTextColor = a.getColor(1, mTabTextColor);

        a.recycle();

        // get custom attrs

        a = context.obtainStyledAttributes(attrs, R.styleable.PagerSlidingTabStrip);

        mIndicatorColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsIndicatorColor, mIndicatorColor);
        mUnderlineColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsUnderlineColor, mUnderlineColor);
        mDividerColor = a.getColor(R.styleable.PagerSlidingTabStrip_pstsDividerColor, mDividerColor);
        mIndicatorHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsIndicatorHeight, mIndicatorHeight);
        mUnderlineHeight = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsUnderlineHeight, mUnderlineHeight);
        mDividerPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsDividerPadding, mDividerPadding);
        mTabPadding = a.getDimensionPixelSize(R.styleable.PagerSlidingTabStrip_pstsTabPaddingLeftRight, mTabPadding);
        mTabBackgroundResId = a.getResourceId(R.styleable.PagerSlidingTabStrip_pstsTabBackground, mTabBackgroundResId);
        mShouldExpand = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsShouldExpand, mShouldExpand);
        mTextAllCaps = a.getBoolean(R.styleable.PagerSlidingTabStrip_pstsTextAllCaps, mTextAllCaps);

        a.recycle();

        mRectPaint = new Paint();
        mRectPaint.setAntiAlias(true);
        mRectPaint.setStyle(Style.FILL);

        mDividerPaint = new Paint();
        mDividerPaint.setAntiAlias(true);
        mDividerPaint.setStrokeWidth(mDividerWidth);

        mDefaultTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        mExpandedTabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);

        if (mLocale == null) {
            mLocale = getResources().getConfiguration().locale;
        }
    }

    public void notifyDataSetChanged() {

        mTabsContainer.removeAllViews();

        mTabCount = mTabs.size();

        for (int i = 0; i < mTabCount; i++) {

            addTab(i, (TabSpec) mTabs.get(i));

        }

        updateTabStyles();

        getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                PagerSlidingTabStrip.this.scrollToChild(PagerSlidingTabStrip.this.mCurrentPosition);
            }
        });

    }

    private void addTab(final int position, final TabSpec tab) {
        View tabView = inflate(getContext(), R.layout.tab_item, null);

        TextView textView1 = ButterKnife.findById(tabView, R.id.tab_value);
        TextView textView2 = ButterKnife.findById(tabView, R.id.tab_label);

        if (TextUtils.isEmpty(tab.mLabel)) {
            throw new IllegalArgumentException("Cannot create tab with an empty label");
        }

        textView1.setText(tab.mLabel);

        LinearLayout linearLayout;

        if (!TextUtils.isEmpty(tab.mValue)) {
            textView2.setText(Html.fromHtml("<b>" + tab.mValue + "</b>"));
        }

        tabView.setFocusable(true);
        tabView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentPosition(position, tab.mId);
            }
        });

        tabView.setPadding(mTabPadding, 0, mTabPadding, 0);
        mTabsContainer.addView(tabView, position, mShouldExpand ? mExpandedTabLayoutParams : mDefaultTabLayoutParams);
    }

    private void updateTabStyles() {

        for (int i = 0; i < mTabCount; i++) {

            View v = mTabsContainer.getChildAt(i);

            v.setBackgroundResource(mTabBackgroundResId);

            if (v instanceof TextView) {

                TextView tab = (TextView) v;
                tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTabTextSize);
                tab.setTypeface(mTabTypeface, mTabTypefaceStyle);
                tab.setTextColor(mTabTextColor);

                // setAllCaps() is only available from API 14, so the upper case is made manually if we are on a
                // pre-ICS-build
                if (mTextAllCaps) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        tab.setAllCaps(true);
                    } else {
                        tab.setText(tab.getText().toString().toUpperCase(mLocale));
                    }
                }
            }
        }

    }

    public void addTab(TabSpec tab) {
        mTabs.add(tab);
    }

    public void bindViewPager(ViewPager viewPager) {
        viewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {
                PagerSlidingTabStrip.this.setCurrentTab(i);
            }
        });
    }

    private void scrollToChild(int position) {

        if (mTabCount == 0) {
            return;
        }

        int newScrollX = mTabsContainer.getChildAt(position).getLeft();

        if (position > 0) {
            newScrollX -= mTabsContainer.getChildAt(position - 1).getWidth();
        }

        if (newScrollX != mLastScrollX) {
            mLastScrollX = newScrollX;
            smoothScrollTo(newScrollX, 0);
        }
    }

    private void setCurrentPosition(int position, int tabId) {
        mCurrentPosition = position;
        scrollToChild(position);

        invalidate();

        if (mOnTabChangeListener != null) {
            mOnTabChangeListener.onTabChanged(tabId);
        }
    }

    public void setOnTabChangeListener(OnTabChangeListener listener) {
        mOnTabChangeListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode() || mTabCount == 0) {
            return;
        }

        final int height = getHeight();

        // draw underline
        if (mDraweUnderline) {
            mRectPaint.setColor(mUnderlineColor);
            canvas.drawRect(0f, height - mUnderlineHeight, mTabsContainer.getWidth(), height, mRectPaint);
        }

        // draw indicator line

        mRectPaint.setColor(mIndicatorColor);

        // default: line below current tab
        View currentTab = mTabsContainer.getChildAt(mCurrentPosition);
        float lineLeft = currentTab.getLeft();
        float lineRight = currentTab.getRight();

        // if there is an offset, start interpolating left and right coordinates between current and next tab
        if (mCurrentPositionOffset > 0f && mCurrentPosition < mTabCount - 1) {

            View nextTab = mTabsContainer.getChildAt(mCurrentPosition + 1);
            final float nextTabLeft = nextTab.getLeft();
            final float nextTabRight = nextTab.getRight();

            lineLeft = (mCurrentPositionOffset * nextTabLeft + (1f - mCurrentPositionOffset) * lineLeft);
            lineRight = (mCurrentPositionOffset * nextTabRight + (1f - mCurrentPositionOffset) * lineRight);
        }

        canvas.drawRect(lineLeft, height - mIndicatorHeight, lineRight, height, mRectPaint);

        // draw divider

        mDividerPaint.setColor(mDividerColor);
        for (int i = 0; i < mTabCount - 1; i++) {
            View tab = mTabsContainer.getChildAt(i);
            canvas.drawLine(tab.getRight(), mDividerPadding, tab.getRight(), height - mDividerPadding, mDividerPaint);
        }
    }

    public void setCurrentTab(int tabId) {
        for (int i = 0, j = mTabs.size(); i < j; i++) {
            TabSpec tab = mTabs.get(i);

            if (tab.mId == tabId) {
                setCurrentPosition(i, tabId);
            }
        }
    }

    public void setIndicatorColor(int mIndicatorColor) {
        this.mIndicatorColor = mIndicatorColor;
        invalidate();
    }

    public void setIndicatorColorResource(int resId) {
        this.mIndicatorColor = getResources().getColor(resId);
        invalidate();
    }

    public int getIndicatorColor() {
        return this.mIndicatorColor;
    }

    public void setIndicatorHeight(int indicatorLineHeightPx) {
        this.mIndicatorHeight = indicatorLineHeightPx;
        invalidate();
    }

    public int getIndicatorHeight() {
        return mIndicatorHeight;
    }

    public void setUnderlineColor(int mUnderlineColor) {
        this.mUnderlineColor = mUnderlineColor;
        invalidate();
    }

    public void setUnderlineColorResource(int resId) {
        this.mUnderlineColor = getResources().getColor(resId);
        invalidate();
    }

    public int getUnderlineColor() {
        return mUnderlineColor;
    }

    public void setDividerColor(int mDividerColor) {
        this.mDividerColor = mDividerColor;
        invalidate();
    }

    public void setDividerColorResource(int resId) {
        this.mDividerColor = getResources().getColor(resId);
        invalidate();
    }

    public int getDividerColor() {
        return mDividerColor;
    }

    public void setUnderlineHeight(int underlineHeightPx) {
        this.mUnderlineHeight = underlineHeightPx;
        invalidate();
    }

    public int getUnderlineHeight() {
        return mUnderlineHeight;
    }

    public void setDividerPadding(int dividerPaddingPx) {
        this.mDividerPadding = dividerPaddingPx;
        invalidate();
    }

    public int getDividerPadding() {
        return mDividerPadding;
    }

    public void setShouldExpand(boolean mShouldExpand) {
        this.mShouldExpand = mShouldExpand;
        requestLayout();
    }

    public boolean getShouldExpand() {
        return mShouldExpand;
    }

    public boolean isTextAllCaps() {
        return mTextAllCaps;
    }

    public void setAllCaps(boolean textAllCaps) {
        this.mTextAllCaps = textAllCaps;
    }

    public void setTextSize(int textSizePx) {
        this.mTabTextSize = textSizePx;
        updateTabStyles();
    }

    public int getTextSize() {
        return mTabTextSize;
    }

    public void setTextColor(int textColor) {
        this.mTabTextColor = textColor;
        updateTabStyles();
    }

    public void setTextColorResource(int resId) {
        this.mTabTextColor = getResources().getColor(resId);
        updateTabStyles();
    }

    public int getTextColor() {
        return mTabTextColor;
    }

    public void setTypeface(Typeface typeface, int style) {
        this.mTabTypeface = typeface;
        this.mTabTypefaceStyle = style;
        updateTabStyles();
    }

    public void setTabBackground(int resId) {
        this.mTabBackgroundResId = resId;
    }

    public int getTabBackground() {
        return mTabBackgroundResId;
    }

    public void setTabPaddingLeftRight(int paddingPx) {
        this.mTabPadding = paddingPx;
        updateTabStyles();
    }

    public int getTabPaddingLeftRight() {
        return mTabPadding;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPosition = savedState.currentPosition;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPosition = mCurrentPosition;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPosition;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPosition);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public TabSpec newTabSpec(int id) {
        return new TabSpec(id);
    }

    public class TabSpec {
        private int mId;
        private String mLabel;
        private String mValue;

        private TabSpec(int id) {
            this.mId = id;
        }

        public TabSpec setLabel(String label) {
            this.mLabel = label;
            return this;
        }
        public TabSpec setValue(String value) {
            this.mValue = value;
            return this;
        }
    }


}