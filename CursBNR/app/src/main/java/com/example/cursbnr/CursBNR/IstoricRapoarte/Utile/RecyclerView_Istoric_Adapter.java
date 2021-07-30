package com.example.cursbnr.CursBNR.IstoricRapoarte.Utile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.cursbnr.CursBNR.IstoricRapoarte.listener.OnRecyclerViewRowClick;
import com.example.cursbnr.R;

import java.util.List;

public class RecyclerView_Istoric_Adapter extends RecyclerView.Adapter<RecyclerView_Istoric_Adapter.ViewHolder> {

    private List<String> tip_moneda;
    private List<String> data_inceput;
    private List<String> data_sfarsit;
    private List<String> tip_raport;
    private Context context;
    private final OnRecyclerViewRowClick onRecyclerViewRowClick;

    public RecyclerView_Istoric_Adapter(List<String> tip_moneda, List<String> data_inceput, List<String> data_sfarsit, List<String> tip_raport, Context context,
                                        OnRecyclerViewRowClick onRecyclerViewRowClick) {
        this.tip_moneda = tip_moneda;
        this.data_inceput = data_inceput;
        this.data_sfarsit = data_sfarsit;
        this.tip_raport = tip_raport;
        this.context = context;
        this.onRecyclerViewRowClick = onRecyclerViewRowClick;
    }

    @Override
    public RecyclerView_Istoric_Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_istoricrapoarte, parent, false);
        return new ViewHolder(view, onRecyclerViewRowClick);
    }

    @Override
    public void onBindViewHolder(RecyclerView_Istoric_Adapter.ViewHolder holder, int position) {
        String tipmoneda = tip_moneda.get(position);
        holder.tvmoneda.setText(tipmoneda);
        String datastart = data_inceput.get(position);
        holder.tvdatastart.setText(datastart);
        String datasfarsit = data_sfarsit.get(position);
        holder.tvdatafinal.setText(datasfarsit);
        String tipraport = tip_raport.get(position);
        holder.tvtipraport.setText(tipraport);
    }


    @Override
    public int getItemCount() {
        return tip_moneda.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvmoneda, tvdatastart, tvdatafinal, tvtipraport;
        private OnRecyclerViewRowClick onRecyclerViewRowClick;

        public ViewHolder(View itemView, OnRecyclerViewRowClick onRecyclerViewRowClick) {
            super(itemView);
            tvmoneda = itemView.findViewById(R.id.tv_tipmoneda);
            tvdatastart = itemView.findViewById(R.id.tv_datainceput);
            tvdatafinal = itemView.findViewById(R.id.tv_datasfarsit);
            tvtipraport = itemView.findViewById(R.id.tv_tipraport);
            this.onRecyclerViewRowClick = onRecyclerViewRowClick;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onRecyclerViewRowClick.onClick(getAbsoluteAdapterPosition());
        }
    }
}
