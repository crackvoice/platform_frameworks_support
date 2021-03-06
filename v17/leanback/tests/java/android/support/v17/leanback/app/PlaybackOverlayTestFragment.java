/*
 * Copyright (C) 2016 The Android Open Source Project
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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.test.R;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

public class PlaybackOverlayTestFragment
        extends PlaybackOverlayFragment
        implements PlaybackOverlayTestActivity.PictureInPictureListener {
    private static final String TAG = "leanback.PlaybackControlsFragment";

    /**
     * Change this to choose a different overlay background.
     */
    private static final int BACKGROUND_TYPE = PlaybackOverlayFragment.BG_LIGHT;

    /**
     * Change the number of related content rows.
     */
    private static final int RELATED_CONTENT_ROWS = 3;

    /**
     * Change this to select hidden
     */
    private static final boolean SECONDARY_HIDDEN = false;

    private static final int ROW_CONTROLS = 0;

    private PlaybackControlHelper mGlue;
    private PlaybackControlsRowPresenter mPlaybackControlsRowPresenter;
    private ListRowPresenter mListRowPresenter;

    private OnItemViewClickedListener mOnItemViewClickedListener = new OnItemViewClickedListener() {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {
            Log.i(TAG, "onItemClicked: " + item + " row " + row);
            if (item instanceof Action) {
                mGlue.onActionClicked((Action) item);
            }
        }
    };

    private OnItemViewSelectedListener mOnItemViewSelectedListener =
            new OnItemViewSelectedListener() {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            Log.i(TAG, "onItemSelected: " + item + " row " + row);
        }
    };

    public SparseArrayObjectAdapter getAdapter() {
        return (SparseArrayObjectAdapter) super.getAdapter();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setBackgroundType(BACKGROUND_TYPE);
        setOnItemViewSelectedListener(mOnItemViewSelectedListener);

        createComponents(getActivity());
    }

    private void createComponents(Context context) {
        mGlue = new PlaybackControlHelper(context, this) {
            @Override
            public int getUpdatePeriod() {
                int totalTime = getControlsRow().getTotalTime();
                if (getView() == null || getView().getWidth() == 0 || totalTime <= 0) {
                    return 1000;
                }
                return Math.max(16, totalTime / getView().getWidth());
            }

            @Override
            protected void onRowChanged(PlaybackControlsRow row) {
                if (getAdapter() == null) {
                    return;
                }
                int index = getAdapter().indexOf(row);
                if (index >= 0) {
                    getAdapter().notifyArrayItemRangeChanged(index, 1);
                }
            }

            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == R.id.lb_control_picture_in_picture) {
                    getActivity().enterPictureInPictureMode();
                    return;
                }
                super.onActionClicked(action);
            }
        };

        mGlue.setOnItemViewClickedListener(mOnItemViewClickedListener);

        mPlaybackControlsRowPresenter = mGlue.createControlsRowAndPresenter();
        mPlaybackControlsRowPresenter.setSecondaryActionsHidden(SECONDARY_HIDDEN);
        mListRowPresenter = new ListRowPresenter();

        setAdapter(new SparseArrayObjectAdapter(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object object) {
                if (object instanceof PlaybackControlsRow) {
                    return mPlaybackControlsRowPresenter;
                } else if (object instanceof ListRow) {
                    return mListRowPresenter;
                }
                throw new IllegalArgumentException("Unhandled object: " + object);
            }
        }));

        // Add the controls row
        getAdapter().set(ROW_CONTROLS, mGlue.getControlsRow());

        // Add related content rows
        for (int i = 0; i < RELATED_CONTENT_ROWS; ++i) {
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new StringPresenter());
            listRowAdapter.add("Some related content");
            listRowAdapter.add("Other related content");
            HeaderItem header = new HeaderItem(i, "Row " + i);
            getAdapter().set(ROW_CONTROLS + 1 + i, new ListRow(header, listRowAdapter));
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        mGlue.setFadingEnabled(true);
        mGlue.enableProgressUpdating(mGlue.hasValidMedia() && mGlue.isMediaPlaying());
        ((PlaybackOverlayTestActivity) getActivity()).registerPictureInPictureListener(this);
    }

    @Override
    public void onStop() {
        mGlue.enableProgressUpdating(false);
        ((PlaybackOverlayTestActivity) getActivity()).unregisterPictureInPictureListener(this);
        super.onStop();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        if (isInPictureInPictureMode) {
            // Hide the controls in picture-in-picture mode.
            setFadingEnabled(true);
            fadeOut();
        } else {
            setFadingEnabled(mGlue.isMediaPlaying());
        }
    }

    abstract static class PlaybackControlHelper extends PlaybackControlGlue {
        /**
         * Change the location of the thumbs up/down controls
         */
        private static final boolean THUMBS_PRIMARY = true;

        private static final String FAUX_TITLE = "A short song of silence";
        private static final String FAUX_SUBTITLE = "2014";
        private static final int FAUX_DURATION = 33 * 1000;

        // These should match the playback service FF behavior
        private static int[] sFastForwardSpeeds = { 2, 3, 4, 5 };

        private boolean mIsPlaying;
        private int mSpeed = PlaybackControlGlue.PLAYBACK_SPEED_PAUSED;
        private long mStartTime;
        private long mStartPosition = 0;

        private PlaybackControlsRow.RepeatAction mRepeatAction;
        private PlaybackControlsRow.ThumbsUpAction mThumbsUpAction;
        private PlaybackControlsRow.ThumbsDownAction mThumbsDownAction;
        private PlaybackControlsRow.PictureInPictureAction mPipAction;
        private static Handler mHandler = new Handler();

        private final Runnable mUpdateProgressRunnable = new Runnable() {
            @Override
            public void run() {
                updateProgress();
                mHandler.postDelayed(this, getUpdatePeriod());
            }
        };

        PlaybackControlHelper(Context context, PlaybackOverlayFragment fragment) {
            super(context, fragment, sFastForwardSpeeds);
            mThumbsUpAction = new PlaybackControlsRow.ThumbsUpAction(context);
            mThumbsUpAction.setIndex(PlaybackControlsRow.ThumbsUpAction.OUTLINE);
            mThumbsDownAction = new PlaybackControlsRow.ThumbsDownAction(context);
            mThumbsDownAction.setIndex(PlaybackControlsRow.ThumbsDownAction.OUTLINE);
            mRepeatAction = new PlaybackControlsRow.RepeatAction(context);
            mPipAction = new PlaybackControlsRow.PictureInPictureAction(context);
        }

        @Override
        public PlaybackControlsRowPresenter createControlsRowAndPresenter() {
            PlaybackControlsRowPresenter presenter = super.createControlsRowAndPresenter();

            ArrayObjectAdapter adapter = new ArrayObjectAdapter(
                    new ControlButtonPresenterSelector());
            getControlsRow().setSecondaryActionsAdapter(adapter);
            if (!THUMBS_PRIMARY) {
                adapter.add(mThumbsDownAction);
            }
            if (android.os.Build.VERSION.SDK_INT > 23) {
                adapter.add(mPipAction);
            }
            adapter.add(mRepeatAction);
            if (!THUMBS_PRIMARY) {
                adapter.add(mThumbsUpAction);
            }

            return presenter;
        }

        @Override
        protected SparseArrayObjectAdapter createPrimaryActionsAdapter(
                PresenterSelector presenterSelector) {
            SparseArrayObjectAdapter adapter = new SparseArrayObjectAdapter(presenterSelector);
            if (THUMBS_PRIMARY) {
                adapter.set(PlaybackControlGlue.ACTION_CUSTOM_LEFT_FIRST, mThumbsUpAction);
                adapter.set(PlaybackControlGlue.ACTION_CUSTOM_RIGHT_FIRST, mThumbsDownAction);
            }
            return adapter;
        }

        @Override
        public void onActionClicked(Action action) {
            if (shouldDispatchAction(action)) {
                dispatchAction(action);
                return;
            }
            super.onActionClicked(action);
        }

        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                Action action = getControlsRow().getActionForKeyCode(keyEvent.getKeyCode());
                if (shouldDispatchAction(action)) {
                    dispatchAction(action);
                    return true;
                }
            }
            return super.onKey(view, keyCode, keyEvent);
        }

        private boolean shouldDispatchAction(Action action) {
            return action == mRepeatAction || action == mThumbsUpAction
                    || action == mThumbsDownAction;
        }

        private void dispatchAction(Action action) {
            Toast.makeText(getContext(), action.toString(), Toast.LENGTH_SHORT).show();
            PlaybackControlsRow.MultiAction multiAction = (PlaybackControlsRow.MultiAction) action;
            multiAction.nextIndex();
            notifyActionChanged(multiAction);
        }

        private void notifyActionChanged(PlaybackControlsRow.MultiAction action) {
            int index;
            index = getPrimaryActionsAdapter().indexOf(action);
            if (index >= 0) {
                getPrimaryActionsAdapter().notifyArrayItemRangeChanged(index, 1);
            } else {
                index = getSecondaryActionsAdapter().indexOf(action);
                if (index >= 0) {
                    getSecondaryActionsAdapter().notifyArrayItemRangeChanged(index, 1);
                }
            }
        }

        private SparseArrayObjectAdapter getPrimaryActionsAdapter() {
            return (SparseArrayObjectAdapter) getControlsRow().getPrimaryActionsAdapter();
        }

        private ArrayObjectAdapter getSecondaryActionsAdapter() {
            return (ArrayObjectAdapter) getControlsRow().getSecondaryActionsAdapter();
        }

        @Override
        public boolean hasValidMedia() {
            return true;
        }

        @Override
        public boolean isMediaPlaying() {
            return mIsPlaying;
        }

        @Override
        public CharSequence getMediaTitle() {
            return FAUX_TITLE;
        }

        @Override
        public CharSequence getMediaSubtitle() {
            return FAUX_SUBTITLE;
        }

        @Override
        public int getMediaDuration() {
            return FAUX_DURATION;
        }

        @Override
        public Drawable getMediaArt() {
            return null;
        }

        @Override
        public long getSupportedActions() {
            return PlaybackControlGlue.ACTION_PLAY_PAUSE
                   | PlaybackControlGlue.ACTION_FAST_FORWARD
                   | PlaybackControlGlue.ACTION_REWIND;
        }

        @Override
        public int getCurrentSpeedId() {
            return mSpeed;
        }

        @Override
        public int getCurrentPosition() {
            int speed;
            if (mSpeed == PlaybackControlGlue.PLAYBACK_SPEED_PAUSED) {
                speed = 0;
            } else if (mSpeed == PlaybackControlGlue.PLAYBACK_SPEED_NORMAL) {
                speed = 1;
            } else if (mSpeed >= PlaybackControlGlue.PLAYBACK_SPEED_FAST_L0) {
                int index = mSpeed - PlaybackControlGlue.PLAYBACK_SPEED_FAST_L0;
                speed = getFastForwardSpeeds()[index];
            } else if (mSpeed <= -PlaybackControlGlue.PLAYBACK_SPEED_FAST_L0) {
                int index = -mSpeed - PlaybackControlGlue.PLAYBACK_SPEED_FAST_L0;
                speed = -getRewindSpeeds()[index];
            } else {
                return -1;
            }
            long position = mStartPosition + (System.currentTimeMillis() - mStartTime) * speed;
            if (position > getMediaDuration()) {
                position = getMediaDuration();
                onPlaybackComplete(true);
            } else if (position < 0) {
                position = 0;
                onPlaybackComplete(false);
            }
            return (int) position;
        }

        void onPlaybackComplete(final boolean ended) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRepeatAction.getIndex() == PlaybackControlsRow.RepeatAction.NONE) {
                        pausePlayback();
                    } else {
                        startPlayback(PlaybackControlGlue.PLAYBACK_SPEED_NORMAL);
                    }
                    mStartPosition = 0;
                    onStateChanged();
                }
            });
        }

        @Override
        protected void startPlayback(int speed) {
            if (speed == mSpeed) {
                return;
            }
            mStartPosition = getCurrentPosition();
            mSpeed = speed;
            mIsPlaying = true;
            mStartTime = System.currentTimeMillis();
        }

        @Override
        protected void pausePlayback() {
            if (mSpeed == PlaybackControlGlue.PLAYBACK_SPEED_PAUSED) {
                return;
            }
            mStartPosition = getCurrentPosition();
            mSpeed = PlaybackControlGlue.PLAYBACK_SPEED_PAUSED;
            mIsPlaying = false;
        }

        @Override
        protected void skipToNext() {
            // Not supported
        }

        @Override
        protected void skipToPrevious() {
            // Not supported
        }

        @Override
        public void enableProgressUpdating(boolean enable) {
            mHandler.removeCallbacks(mUpdateProgressRunnable);
            if (enable) {
                mUpdateProgressRunnable.run();
            }
        }
    }
}
