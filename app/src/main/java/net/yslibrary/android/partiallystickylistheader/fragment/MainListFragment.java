package net.yslibrary.android.partiallystickylistheader.fragment;

import android.os.Bundle;
import android.app.ListFragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import net.yslibrary.android.partiallystickylistheader.R;

import net.yslibrary.android.partiallystickylistheader.fragment.dummy.DummyContent;
import net.yslibrary.android.partiallystickylistheader.util.UIUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainListFragment extends ListFragment implements AbsListView.OnScrollListener {

    private int mActionBarHeight = 0;

    private int mListTop = 0;

    private int minHeaderTranslation = 0;

    View mRootView;

    @InjectView(android.R.id.list)
    ListView mMainList;

    View mHeaderContainer;

    @InjectView(R.id.header_content)
    View mHeaderContent;

    @InjectView(R.id.tab_placeholder)
    View mTabsPlaceHolder;

    @InjectView(R.id.sticky_header)
    RelativeLayout mStickyTabs;


    public static MainListFragment newInstance() {
        MainListFragment fragment = new MainListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MainListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
        }


        setListAdapter(new ArrayAdapter<DummyContent.DummyItem>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, DummyContent.ITEMS));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_main_list, container, false);

        final ListView localListView = (ListView) mRootView.findViewById(android.R.id.list);

        mHeaderContainer = inflater.inflate(R.layout.list_header, localListView, false);

        localListView.addHeaderView(mHeaderContainer);

        ButterKnife.inject(this, mRootView);

        getActionBarHeight();

        localListView.post(new Runnable() {
            @Override
            public void run() {
                mListTop = getListView().getTop();
            }
        });

        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().setOnScrollListener(this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }


    /**
     * ActionBarの高さ判定
     * @return
     */
    public int getActionBarHeight() {
        if (mActionBarHeight != 0) {
            return mActionBarHeight;
        }

        mActionBarHeight = UIUtil.calculateActionBarSize(getActivity());

        return mActionBarHeight;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        int actionBarHeight = getActionBarHeight();

        if (firstVisibleItem <= 1) {

            actionBarHeight = Math.max(mTabsPlaceHolder.getTop() + mHeaderContainer.getTop(), mActionBarHeight);
        } else {
            mStickyTabs.setTranslationY(actionBarHeight);
        }
    }

    /**
     * xがminより小さかったらminを、maxより大きければmaxを、min - maxの範囲内だったらxを返す
     * @param x
     * @param min
     * @param max
     * @return
     */
    private float clamp(float x, float min, float max) {
        return Math.max(Math.min(x, max), min);
    }

    private int getHeaderScrollY() {
        View localView = getListView().getChildAt(0);
        if (localView == null) {
            return 0;
        }

        int i = getListView().getFirstVisiblePosition();
        int j = localView.getTop();
        int k = 0;

        if (i >= 1) {
            k = (int) this.mHeaderContainer.getHeight();
        }

        return k + (-j + i * localView.getHeight());
    }
}
