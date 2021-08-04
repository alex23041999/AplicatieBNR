package com.example.cursbnr.Inventar.Utile;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.cursbnr.CursBNR.GenerareRapoarte.Utile.DateBaseHelper;
import com.example.cursbnr.Inventar.Inventar;
import com.example.cursbnr.Inventar.Listener.OnRecyclerViewRow;
import com.example.cursbnr.R;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewInventar_Adapter extends RecyclerView.Adapter<RecyclerViewInventar_Adapter.ViewHolder> {

    Context context;
    private List<ObjectInventar> objectsInventar;
    boolean modify = false;
    private final OnRecyclerViewRow onRecyclerViewRow;
    private DateBaseHelper dateBaseHelper;

    public RecyclerViewInventar_Adapter(List<ObjectInventar> objectsInventar, Context context, OnRecyclerViewRow onRecyclerViewRow) {
        this.context = context;
        this.objectsInventar = objectsInventar;
        this.onRecyclerViewRow = onRecyclerViewRow;
    }

    @Override
    public RecyclerViewInventar_Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_inventar, parent, false);
        return new ViewHolder(view, onRecyclerViewRow);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String list_den = objectsInventar.get(position).getDenumire();
        holder.tvdenumire.setText(list_den);
        Float list_pret = objectsInventar.get(position).getPret();
        holder.tvpret.setText(list_pret.toString());
        String list_cod = objectsInventar.get(position).getCodbare();
        holder.tvcodbare.setText(list_cod);
        Float list_cant = objectsInventar.get(position).getCantitate();
        holder.etcantitate.setText(list_cant.toString());

    }

    @Override
    public int getItemCount() {
        return objectsInventar.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvdenumire, tvpret, tvcodbare;
        public EditText etcantitate;
        private OnRecyclerViewRow onRecyclerViewRow;

        public ViewHolder(View itemView, OnRecyclerViewRow onRecyclerViewRow) {
            super(itemView);
            tvdenumire = itemView.findViewById(R.id.denumire_produs);
            tvpret = itemView.findViewById(R.id.pret_produs);
            tvcodbare = itemView.findViewById(R.id.codbare_produs);
            etcantitate = itemView.findViewById(R.id.cantitate_produs);
           etcantitate.addTextChangedListener(new DecimalInputTextWatcher(etcantitate, 2));

            etcantitate.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                            || actionId == EditorInfo.IME_ACTION_DONE){
                            try {
                                ObjectInventar obj = objectsInventar.get(getAbsoluteAdapterPosition());
                                obj.setCantitate(Float.valueOf((etcantitate.getText().toString())));
                                new DateBaseHelper(context).updateDataBaseInventar(obj.getDenumire(), obj.getCantitate());
                                notifyDataSetChanged();
                                etcantitate.clearFocus();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                    return false;
                }
            });

//            etcantitate.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                }
//
//                @Override
//                public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    modify = true;
//                }
//
//                @Override
//                public void afterTextChanged(Editable s) {
//                    if (modify) {
//                        modify = false;
//                        try {
//                            objectsInventar.get(getAdapterPosition()).setCantitate(Float.valueOf((etcantitate.getText().toString())));
//                            notifyDataSetChanged();
//                            dateBaseHelper.updateDataBaseInventar(objectsInventar.get(getAdapterPosition()).getDenumire(),Float.valueOf(etcantitate.getText().toString()));
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
//            });

            this.onRecyclerViewRow = onRecyclerViewRow;
            itemView.setOnClickListener(this);
        }

        public void onClick(View v) {
            onRecyclerViewRow.onClick(getAbsoluteAdapterPosition());
            etcantitate.requestFocus();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(etcantitate, InputMethodManager.SHOW_IMPLICIT);
            etcantitate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    // If it loses focus...
                    if (!hasFocus) {
                        // Hide soft keyboard.
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etcantitate.getWindowToken(), 0);
                        // Make it non-editable again.
                        etcantitate.setKeyListener(null);
                    }
                }
            });
        }
    }
    public void filterList(ArrayList<ObjectInventar> filteredList) {
        objectsInventar = filteredList;
        notifyDataSetChanged();
    }
}
