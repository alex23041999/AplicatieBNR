package com.example.cursbnr.GenerareRapoarte.Utile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cursbnr.R;

import java.util.List;

public class RecyclerView_TipLista_Adapter extends RecyclerView.Adapter<RecyclerView_TipLista_Adapter.ViewHolder> {

    private List<String> monede;
    private List<String> intervale;
    private List<Float> minim;
    private List<Float> maxim;
    private Context context;

    public RecyclerView_TipLista_Adapter(List<String> monede,List<String> intervale,List<Float>minim,List<Float>maxim,Context context){
        this.monede = monede;
        this.intervale = intervale;
        this.minim = minim;
        this.maxim = maxim;
        this.context = context;
    }
    @Override
    public RecyclerView_TipLista_Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_tiplista,parent, false);
       return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView_TipLista_Adapter.ViewHolder holder, int position) {
        String list_monede = monede.get(position);
        holder.tvmoneda.setText(list_monede);
        String list_interval = intervale.get(position);
        holder.tvinterval.setText(list_interval);
        Float list_minim = minim.get(position);
        holder.tvmin.setText(list_minim.toString());
        Float list_maxim = maxim.get(position);
        holder.tvmax.setText(list_maxim.toString());
    }

    @Override
    public int getItemCount() {
        while (maxim.size()< monede.size())
            maxim.add(Float.valueOf(0));
        while(monede.size()<maxim.size())
            monede.add("Moneda nevalabila");
        if(monede.size()>=maxim.size())
        {return monede.size();}
        else {return maxim.size();}
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView tvmoneda,tvinterval,tvmin,tvmax;
        public ViewHolder(View itemView){
            super(itemView);
            tvmoneda = itemView.findViewById(R.id.tv_tiplista_moneda);
            tvinterval = itemView.findViewById(R.id.tv_tiplista_interval);
            tvmin = itemView.findViewById(R.id.tv_tiplista_minim);
            tvmax = itemView.findViewById(R.id.tv_tiplista_maxim);
        }

    }
}
