package com.example.cursbnr.CursBNR.CursValutar.Utile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.cursbnr.R;

import java.util.List;

public class RecyclerCurs_Adapter extends RecyclerView.Adapter<RecyclerCurs_Adapter.ViewHolder> {

    private Context context;
    private List<String> monede;
    private List<String> valori;
    private int[] images;

    public RecyclerCurs_Adapter(List<String> monede, List<String> valori, int[] images, Context context) {
        this.context = context;
        this.monede = monede;
        this.valori = valori;
        this.images = images;
    }

    @Override
    public RecyclerCurs_Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclewview_cursvalutar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerCurs_Adapter.ViewHolder holder, int position) {
        String listmonede = monede.get(position);
        holder.tvmoneda.setText(listmonede);
        String listvalori = valori.get(position);
        holder.tvvaloare.setText(listvalori);
        holder.imsteag.setImageResource(images[position]);
    }

    @Override
    public int getItemCount() {
        while (valori.size() < monede.size())
            valori.add("Valoare nevalabila");
        while (monede.size() < valori.size())
            monede.add("Moneda nevalabila");
        if (monede.size() >= valori.size())
            return monede.size();
        else return valori.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvmoneda, tvvaloare;
        public ImageView imsteag;

        public ViewHolder(View itemView) {
            super(itemView);
            tvmoneda = itemView.findViewById(R.id.tv_moneda);
            tvvaloare = itemView.findViewById(R.id.tv_valoare);
            imsteag = itemView.findViewById(R.id.im_steag);
        }
    }
}
