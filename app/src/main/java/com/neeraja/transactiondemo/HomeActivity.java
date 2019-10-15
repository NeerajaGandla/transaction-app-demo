package com.neeraja.transactiondemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.neeraja.transactiondemo.data.Transaction;
import com.neeraja.transactiondemo.utils.ApiUtils;
import com.neeraja.transactiondemo.utils.Constants;
import com.neeraja.transactiondemo.utils.CustomException;
import com.neeraja.transactiondemo.utils.Globals;
import com.neeraja.transactiondemo.utils.HttpRequest;
import com.neeraja.transactiondemo.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context mContext;
    private ProgressDialog progressDialog;
    private String displayBalance;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        sharedPreferences = getSharedPreferences(Constants.myPref, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.clear();
        new LoginAsync().execute();
    }

    public class LoginAsync extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                JSONObject response = (JSONObject) HttpRequest.postData(ApiUtils.getLoginUrl(), null, JSONObject.class, getApplicationContext());

                if (response != null) {
                    String token = response.getString(Constants.TOKEN);
                    if (Utils.isValidString(token)) {
                        editor.putString(Constants.TOKEN, token);
                        editor.commit();
                    }
                }
            } catch (CustomException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            String token = sharedPreferences.getString(Constants.TOKEN, null);
            if (Utils.isValidString(token)) {

            }
            super.onPostExecute(o);
        }
    }

    public class GetBalanceAsync extends AsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = Utils.getProgressDialog(mContext);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Globals.lastErrMsg = "";
                JSONObject response = (JSONObject) HttpRequest.getInputStreamFromUrl(ApiUtils.getBalanceUrl(), JSONObject.class, mContext);
                if (response != null) {
                    String balance = response.getString("balance");
                    String currency = response.getString("currency");
                    displayBalance = (balance != null ? balance : "--") + (currency != null ? currency : "--");

                } else {
                    Globals.lastErrMsg = Constants.DATA_UNAVAILABLE;
                }
            } catch (CustomException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (progressDialog != null && progressDialog.isShowing())
                Utils.dismissProgressDialog();


            super.onPostExecute(o);
        }
    }
    public class GetTransactionsAsync extends AsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = Utils.getProgressDialog(mContext);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Globals.lastErrMsg = "";
                JSONObject response = (JSONObject) HttpRequest.getInputStreamFromUrl(ApiUtils.getTransactionsUrl(), JSONObject.class, mContext);
                if (response != null) {
                    String balance = response.getString("balance");
                    String currency = response.getString("currency");
                    displayBalance = (balance != null ? balance : "--") + (currency != null ? currency : "--");

                } else {
                    Globals.lastErrMsg = Constants.DATA_UNAVAILABLE;
                }
            } catch (CustomException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (progressDialog != null && progressDialog.isShowing())
                Utils.dismissProgressDialog();


            super.onPostExecute(o);
        }
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.DataObjectHolder> {
        private ArrayList<Transaction> list;

        public class DataObjectHolder extends RecyclerView.ViewHolder {
            TextView pickupNumTv;
            TextView consignorNameTv;
            TextView pcsWtTv, pcsTv;
            TextView originTv, destTv;

            Button bookBtn, submitPickupBtn;
            Button closeBtn, rescheduleBtn;

            public DataObjectHolder(View itemView) {
                super(itemView);
                pickupNumTv = (TextView) itemView.findViewById(R.id.tv_pickup_num);
                consignorNameTv = (TextView) itemView.findViewById(R.id.tv_consignor_name);
                pcsTv = (TextView) itemView.findViewById(R.id.tv_no_of_articles);
                pcsWtTv = (TextView) itemView.findViewById(R.id.tv_weight);
                originTv = (TextView) itemView.findViewById(R.id.tv_origin);
                destTv = (TextView) itemView.findViewById(R.id.tv_destination);

                bookBtn = (Button) itemView.findViewById(R.id.btn_book);
                bookBtn.setOnClickListener(this);

                rescheduleBtn = (Button) itemView.findViewById(R.id.btn_reschedule);
                submitPickupBtn = (Button) itemView.findViewById(R.id.btn_submit_pickup);
                submitPickupBtn.setOnClickListener(this);
                rescheduleBtn.setOnClickListener(this);
                closeBtn = (Button) itemView.findViewById(R.id.btn_close);
                closeBtn.setOnClickListener(this);
                if (status == 0 || status == 1) {
                    bookBtn.setVisibility(View.VISIBLE);
                } else if (status == 4) {
                    bookBtn.setVisibility(View.GONE);
                    submitPickupBtn.setVisibility(View.GONE);
                }
                if (status == 1) {
                    bookBtn.setText("Add Dockets");
                    submitPickupBtn.setVisibility(View.VISIBLE); //VISIBLE
                } else {
                    bookBtn.setText("Book");
                    submitPickupBtn.setVisibility(View.GONE);
                }
            }
        }

        public MyAdapter(ArrayList<Transaction> myDataset) {
            list = myDataset;
        }

        @Override
        public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pickup_data, parent, false);

            DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
            return dataObjectHolder;
        }

        @Override
        public void onBindViewHolder(DataObjectHolder holder, int position) {
            Transaction data = list.get(position);
            holder.pickupNumTv.setText(data.getPickupNumber());
            holder.consignorNameTv.setText(data.getConsignorName());
            holder.pcsTv.setText(data.getPackagesCount() + "");
            holder.pcsWtTv.setText(data.getWeight() + "");
            holder.originTv.setText(data.getOrigin());
            holder.destTv.setText(data.getDestination());

            holder.bookBtn.setTag(position);
            holder.rescheduleBtn.setTag(position);
            holder.submitPickupBtn.setTag(position);
            holder.closeBtn.setTag(position);
        }

        @Override
        public int getItemCount() {
            count = list.size();
            return list.size();
        }

    }
}
