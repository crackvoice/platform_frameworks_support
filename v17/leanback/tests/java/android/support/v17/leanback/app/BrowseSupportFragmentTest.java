// CHECKSTYLE:OFF Generated code
/* This file is auto-generated from BrowseFragmentTest.java.  DO NOT MODIFY. */

/*
 * Copyright (C) 2015 The Android Open Source Project
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
package android.support.v17.leanback.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v17.leanback.widget.ItemBridgeAdapter;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class BrowseSupportFragmentTest {

    static final String TAG = "BrowseSupportFragmentTest";
    static final long TRANSITION_LENGTH = 1000;
    static final long HORIZONTAL_SCROLL_WAIT = 2000;

    @Rule
    public ActivityTestRule<BrowseSupportFragmentTestActivity> activityTestRule =
            new ActivityTestRule<>(BrowseSupportFragmentTestActivity.class, false, false);
    private BrowseSupportFragmentTestActivity mActivity;

    @After
    public void afterTest() throws Throwable {
        activityTestRule.runOnUiThread(new Runnable() {
            public void run() {
                if (mActivity != null) {
                    mActivity.finish();
                    mActivity = null;
                }
            }
        });
    }

    @Test
    public void testTwoBackKeysWithBackStack() throws Throwable {
        final long dataLoadingDelay = 1000;
        Intent intent = new Intent();
        intent.putExtra(BrowseSupportFragmentTestActivity.EXTRA_LOAD_DATA_DELAY, dataLoadingDelay);
        intent.putExtra(BrowseSupportFragmentTestActivity.EXTRA_ADD_TO_BACKSTACK , true);
        mActivity = activityTestRule.launchActivity(intent);

        Thread.sleep(dataLoadingDelay + TRANSITION_LENGTH);

        assertNotNull(mActivity.getBrowseTestSupportFragment().getMainFragment());
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        Thread.sleep(TRANSITION_LENGTH);
        sendKeys(KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_BACK);
    }

    @Test
    public void testTwoBackKeysWithoutBackStack() throws Throwable {
        final long dataLoadingDelay = 1000;
        Intent intent = new Intent();
        intent.putExtra(BrowseSupportFragmentTestActivity.EXTRA_LOAD_DATA_DELAY, dataLoadingDelay);
        intent.putExtra(BrowseSupportFragmentTestActivity.EXTRA_ADD_TO_BACKSTACK , false);
        mActivity = activityTestRule.launchActivity(intent);

        Thread.sleep(dataLoadingDelay + TRANSITION_LENGTH);

        assertNotNull(mActivity.getBrowseTestSupportFragment().getMainFragment());
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
        Thread.sleep(TRANSITION_LENGTH);
        sendKeys(KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_BACK);
    }

    @Test
    public void testPressRightBeforeMainFragmentCreated() throws Throwable {
        final long dataLoadingDelay = 1000;
        Intent intent = new Intent();
        intent.putExtra(BrowseSupportFragmentTestActivity.EXTRA_LOAD_DATA_DELAY, dataLoadingDelay);
        intent.putExtra(BrowseSupportFragmentTestActivity.EXTRA_ADD_TO_BACKSTACK , false);
        mActivity = activityTestRule.launchActivity(intent);

        assertNull(mActivity.getBrowseTestSupportFragment().getMainFragment());
        sendKeys(KeyEvent.KEYCODE_DPAD_RIGHT);
    }

    @Test
    public void testSelectCardOnARow() throws Throwable {
        final int selectRow = 10;
        final int selectItem = 20;
        Intent intent = new Intent();
        final long dataLoadingDelay = 1000;
        intent.putExtra(BrowseSupportFragmentTestActivity.EXTRA_LOAD_DATA_DELAY, dataLoadingDelay);
        intent.putExtra(BrowseSupportFragmentTestActivity.EXTRA_ADD_TO_BACKSTACK , true);
        mActivity = activityTestRule.launchActivity(intent);

        Thread.sleep(dataLoadingDelay + TRANSITION_LENGTH);

        Presenter.ViewHolderTask itemTask = Mockito.spy(
                new ItemSelectionTask(mActivity, selectRow));

        final ListRowPresenter.SelectItemViewHolderTask task =
                new ListRowPresenter.SelectItemViewHolderTask(selectItem);
        task.setItemTask(itemTask);

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.getBrowseTestSupportFragment().setSelectedPosition(selectRow, true, task);
            }
        });

        verify(itemTask, timeout(5000).times(1)).run(any(Presenter.ViewHolder.class));

        activityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ListRowPresenter.ViewHolder row = (ListRowPresenter.ViewHolder) mActivity
                        .getBrowseTestSupportFragment().getRowsSupportFragment().getRowViewHolder(selectRow);
                assertNotNull(dumpRecyclerView(mActivity.getBrowseTestSupportFragment().getGridView()), row);
                assertNotNull(row.getGridView());
                assertEquals(selectItem, row.getGridView().getSelectedPosition());
            }
        });
    }

    @Test
    public void activityRecreate_notCrash() throws Throwable {
        final long dataLoadingDelay = 1000;
        Intent intent = new Intent();
        intent.putExtra(BrowseSupportFragmentTestActivity.EXTRA_LOAD_DATA_DELAY, dataLoadingDelay);
        intent.putExtra(BrowseSupportFragmentTestActivity.EXTRA_ADD_TO_BACKSTACK , false);
        intent.putExtra(BrowseSupportFragmentTestActivity.EXTRA_SET_ADAPTER_AFTER_DATA_LOAD, true);
        mActivity = activityTestRule.launchActivity(intent);

        Thread.sleep(dataLoadingDelay + TRANSITION_LENGTH);

        InstrumentationRegistry.getInstrumentation().callActivityOnRestart(mActivity);
        activityTestRule.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.recreate();
            }
        });
    }

    private void sendKeys(int ...keys) {
        for (int i = 0; i < keys.length; i++) {
            InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(keys[i]);
        }
    }

    public static class ItemSelectionTask extends Presenter.ViewHolderTask {

        private final BrowseSupportFragmentTestActivity activity;
        private final int expectedRow;

        public ItemSelectionTask(BrowseSupportFragmentTestActivity activity, int expectedRow) {
            this.activity = activity;
            this.expectedRow = expectedRow;
        }

        public void run(Presenter.ViewHolder holder) {
            android.util.Log.d(TAG, dumpRecyclerView(activity.getBrowseTestSupportFragment()
                    .getGridView()));
            android.util.Log.d(TAG, "Row " + expectedRow + " " + activity.getBrowseTestSupportFragment()
                    .getRowsSupportFragment().getRowViewHolder(expectedRow), new Exception());
        }
    }

    static String dumpRecyclerView(RecyclerView recyclerView) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < recyclerView.getChildCount(); i++) {
            View child = recyclerView.getChildAt(i);
            ItemBridgeAdapter.ViewHolder vh = (ItemBridgeAdapter.ViewHolder)
                    recyclerView.getChildViewHolder(child);
            b.append("child").append(i).append(":").append(vh);
            if (vh != null) {
                b.append(",").append(vh.getViewHolder());
            }
            b.append(";");
        }
        return b.toString();
    }
}
