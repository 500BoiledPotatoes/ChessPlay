package com.example.chessplay;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<RecBook> data;
    private Context context;

    public MyAdapter(List<RecBook> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.recycle_item, null);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            holder.title.setText(data.get(position).getTitle());
            holder.author.setText(data.get(position).getAuthor());
            holder.image.setImageResource(data.get(position).getImageId());

    }


    @Override
    public int getItemCount() {
        return data == null? 0 : data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView author;
        private ImageView image;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.fruit_name);
            author = itemView.findViewById(R.id.fruit_color);
            image = itemView.findViewById(R.id.fruit_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onRecyclerItemClickListener != null){
                        onRecyclerItemClickListener.onRecyclerItemClick(getAdapterPosition());
                    }
                }
            });

        }
    }

    private OnRecyclerItemClickListener onRecyclerItemClickListener;

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener listener){
        onRecyclerItemClickListener = listener;
    }
    public interface  OnRecyclerItemClickListener{
        void onRecyclerItemClick(int position);
    }


}
