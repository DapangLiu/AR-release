package edu.asu.artag.UI;


import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.clans.fab.FloatingActionButton;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import edu.asu.artag.R;


public class FloatingProgressButton extends Fragment {

    private int mScrollOffset = 4;
    private int mMaxProgress = 100;
    private LinkedList<ProgressType> mProgressTypes;
    private Handler mUiHandler = new Handler();
    private handleFPBclick mActivity;

    public interface handleFPBclick {

        void savePic() throws IOException;
        void base64Encode() throws FileNotFoundException;
        void VolleyUpload();
        void startScreenShot();

    }



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mActivity = (handleFPBclick) getActivity();
        return inflater.inflate(R.layout.floating_pgb_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressTypes = new LinkedList<>();
        for (ProgressType type : ProgressType.values()) {
            mProgressTypes.offer(type);
        }

        final FloatingActionButton fgb = (FloatingActionButton) view.findViewById(R.id.fgb);
        fgb.setMax(mMaxProgress);

        fgb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fgb.setShowProgressBackground(true);
                increaseProgress(fgb,0);

                mActivity.startScreenShot();

                try {
                    mActivity.savePic();
                    mActivity.base64Encode();
                    mActivity.VolleyUpload();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


        });


    }


    private void increaseProgress(final FloatingActionButton fgb, int i) {
        if (i <= mMaxProgress) {
            fgb.setProgress(i, false);
            final int progress = ++i;
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    increaseProgress(fgb, progress);
                }
            }, 30);
        } else {
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fgb.hideProgress();
                }
            }, 200);
            mProgressTypes.offer(ProgressType.PROGRESS_NO_ANIMATION);
        }
    }

//    private class LanguageAdapter extends RecyclerView.Adapter<ViewHolder> {
//
//        private Locale[] mLocales;
//
//        private LanguageAdapter(Locale[] mLocales) {
//            this.mLocales = mLocales;
//        }
//
//        @Override
//        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            TextView tv = (TextView) LayoutInflater.from(parent.getContext())
//                    .inflate(android.R.layout.simple_list_item_1, parent, false);
//
//            return new ViewHolder(tv);
//        }
//
//        @Override
//        public void onBindViewHolder(ViewHolder holder, int position) {
//            holder.mTextView.setText(mLocales[position].getDisplayName());
//        }
//
//        @Override
//        public int getItemCount() {
//            return mLocales.length;
//        }
//    }

//    private static class ViewHolder extends RecyclerView.ViewHolder {
//
//        public TextView mTextView;
//
//        public ViewHolder(TextView v) {
//            super(v);
//            mTextView = v;
//        }
//    }

    private enum ProgressType {
        INDETERMINATE, PROGRESS_POSITIVE, PROGRESS_NEGATIVE, HIDDEN, PROGRESS_NO_ANIMATION, PROGRESS_NO_BACKGROUND
    }



}
