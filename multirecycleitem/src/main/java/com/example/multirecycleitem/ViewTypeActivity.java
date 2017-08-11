package com.example.multirecycleitem;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ViewTypeActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private List<String> mDatas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.id_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //mRecyclerView.addItemDecoration(new SpacesItemDecoration(12));
        mRecyclerView.setAdapter(new ViewTypeAdapter());
    }

    private void initData() {
        mDatas = new ArrayList<String>();
        for (int i = 0; i < 24; i++) {
            mDatas.add("位置是" + i);
        }
    }

    class ViewTypeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        public static final int ONE_ITEM = 1;
        public static final int TWO_ITEM = 2;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater mInflater = LayoutInflater.from(ViewTypeActivity.this);
            RecyclerView.ViewHolder holder = null;
            if (ONE_ITEM == viewType) {
                View v = mInflater.inflate(R.layout.item_image_view, parent, false);
                holder = new OneViewHolder(v);
            } else {
                View v = mInflater.inflate(R.layout.item_img_text_view, parent, false);
                holder = new TwoViewHolder(v);
            }
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof OneViewHolder) {
//                ((OneViewHolder) holder).tv.setText(mDatas.get(position));
            } else {
//                ((TwoViewHolder) holder).tv1.setText(mDatas.get(position));
//                ((TwoViewHolder) holder).tv2.setText(mDatas.get(position));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position % 2 == 0) {
                return TWO_ITEM;
            } else {
                return ONE_ITEM;
            }
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        class OneViewHolder extends RecyclerView.ViewHolder {
            TextView tv;

            public OneViewHolder(View itemView) {
                super(itemView);
                //tv = (TextView) itemView.findViewById(R.id.adapter_linear_text);
            }
        }

        class TwoViewHolder extends RecyclerView.ViewHolder {
            TextView tv1, tv2;

            public TwoViewHolder(View itemView) {
                super(itemView);
                //tv1 = (TextView) itemView.findViewById(R.id.adapter_two_1);
                //tv2 = (TextView) itemView.findViewById(R.id.adapter_two_2);
            }
        }
    }
}
