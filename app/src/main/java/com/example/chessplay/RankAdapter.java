package com.example.chessplay;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.lwjgl.Sys;

import java.text.DecimalFormat;
import java.util.List;

public class RankAdapter extends RecyclerView.Adapter<RankAdapter.MyViewHolder> {
    private List<User> data;
    private Context context;

    public RankAdapter(List<User> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(context, R.layout.recycle_item1, null);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

            holder.name.setText(data.get(position).getUsername() );
            DecimalFormat decimalFormat= new  DecimalFormat( "0.00" );
            String rate = decimalFormat.format((float) (data.get(position).getBwin()+data.get(position).getWwin())/(data.get(position).getBwin()+data.get(position).getWwin()+data.get(position).getbLose()+data.get(position).getwLose())*100);
            holder.rate.setText(String.valueOf(rate)+ "%");
    }


    @Override
    public int getItemCount() {
        return data == null? 0 : data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView rate;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.username);
            rate = itemView.findViewById(R.id.rate);

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
