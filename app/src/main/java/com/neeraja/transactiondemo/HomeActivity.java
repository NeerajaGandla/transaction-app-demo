package com.neeraja.transactiondemo;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.neeraja.transactiondemo.data.BalanceResponse;
import com.neeraja.transactiondemo.data.LoginResponse;
import com.neeraja.transactiondemo.data.Transaction;
import com.neeraja.transactiondemo.utils.ApiUtils;
import com.neeraja.transactiondemo.utils.Constants;
import com.neeraja.transactiondemo.utils.CustomException;
import com.neeraja.transactiondemo.utils.Globals;
import com.neeraja.transactiondemo.utils.HttpRequest;
import com.neeraja.transactiondemo.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context mContext;
    private ProgressDialog progressDialog;
    private double availableBalance;
    private String displayBalance;
    private List<Transaction> transactionList = new ArrayList<>();
    private int count;
    @BindView(R.id.tv_available_balance)
    TextView balanceTv;
    @BindView(R.id.btn_transaction)
    Button makeTransactionBtn;
    @BindView(R.id.rv_transactions)
    RecyclerView transactionRv;
    private TransactionAdapter adapter;
    @BindView(R.id.layout_transaction)
    CardView transactionLayout;
    @BindView(R.id.btn_cancel)
    Button cancelBtn;
    @BindView(R.id.btn_proceed)
    Button proceedBtn;
    @BindView(R.id.et_amount)
    EditText amountEt;
    @BindView(R.id.et_description)
    EditText descriptionEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        mContext = HomeActivity.this;
        sharedPreferences = getSharedPreferences(Constants.myPref, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.clear();
        transactionRv.setLayoutManager(new LinearLayoutManager(mContext));
        transactionRv.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        adapter = new TransactionAdapter((ArrayList<Transaction>) transactionList);
        transactionRv.setAdapter(adapter);
        if (Utils.getConnectivityStatus(mContext)) {
            new LoginAsync().execute();
        } else {
            Toast.makeText(mContext, getString(R.string.internet_connection_msg), Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.btn_transaction)
    public void onTransactionClick(View view) {
        transactionLayout.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.btn_cancel)
    public void onCancelClick(View view) {
        amountEt.setText(null);
        descriptionEt.setText(null);
        transactionLayout.setVisibility(View.GONE);
    }

    @OnClick(R.id.btn_proceed)
    public void onProceedClick(View view) {
        String amount = amountEt.getText().toString().trim();
        String description = descriptionEt.getText().toString().trim();
        if (Utils.isValidString(amount)) {
            double amt = 0;
            try {
                amt = Double.parseDouble(amount);
            } catch (NumberFormatException nfe) {
                showAlert(getString(R.string.invalid_amount));
                return;
            }
            if (amt < availableBalance) {
                if (Utils.isValidString(description)) {
                    transactionLayout.setVisibility(View.GONE);
                    Transaction transaction = new Transaction();
                    transaction.setAmount(amount);
                    transaction.setDescription(description);
                    transaction.setCurrency(getString(R.string.gbp));//assumed to be static as of now
                    transaction.setDate(getTodaysDate());
                    if (Utils.getConnectivityStatus(mContext)) {
                        new SaveTransactionAsync(transaction).execute();
                    } else {
                        showAlert(getString(R.string.internet_connection_msg));
                    }
                } else {
                    showAlert(getString(R.string.valid_description));
                }
            } else {
                showAlert(getString(R.string.low_balance));
            }
        } else {
            showAlert(getString(R.string.valid_amount));
        }
    }

    private String getTodaysDate() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date dateobj = new Date();
        return df.format(dateobj);
    }

    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(message);
        builder.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog d = builder.create();
        d.show();
    }

    public class LoginAsync extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                LoginResponse response = (LoginResponse) HttpRequest.postData(ApiUtils.getLoginUrl(), "", LoginResponse.class, getApplicationContext());
                if (response != null) {
                    Log.d(TAG, "doInBackground: " + response.toString());
                    String token = response.getToken();
                    if (Utils.isValidString(token)) {
                        editor.putString(Constants.TOKEN, token);
                        editor.commit();
                    }
                }
            } catch (CustomException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            String token = sharedPreferences.getString(Constants.TOKEN, null);
            if (Utils.isValidString(token)) {
                if (Utils.getConnectivityStatus(mContext)) {
                    new GetBalanceAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    new GetTransactionsAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    Toast.makeText(mContext, getString(R.string.internet_connection_msg), Toast.LENGTH_SHORT).show();
                }
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
                BalanceResponse response = (BalanceResponse) HttpRequest.getInputStreamFromUrl(ApiUtils.getBalanceUrl(), BalanceResponse.class, mContext);
                if (response != null) {
                    String balance = response.getBalance();
                    String currency = response.getCurrency();
                    try {
                        availableBalance = Double.parseDouble(balance);
                    } catch (NumberFormatException nfe) {
                        availableBalance = 0.0;
                    }
                    displayBalance = (balance != null ? balance : "--") + (currency != null ? currency : "--");

                } else {
                    Globals.lastErrMsg = Constants.DATA_UNAVAILABLE;
                }
            } catch (CustomException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (progressDialog != null && progressDialog.isShowing())
                Utils.dismissProgressDialog();
            if (Utils.isValidString(displayBalance))
                balanceTv.setText(displayBalance);

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
                String responseStr = (String) HttpRequest.getInputStreamFromUrl(ApiUtils.getTransactionsUrl(), mContext);
                Type typeMyType = new TypeToken<ArrayList<Transaction>>(){}.getType();
                Gson gson = new Gson();
                transactionList = gson.fromJson(responseStr.toString(), typeMyType);

                if (transactionList != null) {
                    Log.d(TAG, "doInBackground: " + transactionList.toString());
                }



            } catch (CustomException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (progressDialog != null && progressDialog.isShowing())
                Utils.dismissProgressDialog();
            if (Utils.isValidString(Globals.lastErrMsg)) {
                showAlert(Globals.lastErrMsg);
            } else if (Utils.isValidArrayList((ArrayList<?>) transactionList)) {
                Log.d(TAG, "onPostExecute: "+ transactionList.toString());
                adapter.updateData((ArrayList<Transaction>) transactionList);
                adapter.notifyDataSetChanged();
            }
            super.onPostExecute(o);
        }
    }

    public class SaveTransactionAsync extends AsyncTask {
        Transaction transaction;

        public SaveTransactionAsync(Transaction transaction) {
            this.transaction = transaction;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Gson gson = new Gson();
                Globals.lastErrMsg = "";
                String jsonStr = gson.toJson(transaction);
                JSONObject response = (JSONObject) HttpRequest.postData(ApiUtils.getSpendUrl(), jsonStr, JSONObject.class, getApplicationContext());
            } catch (CustomException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (progressDialog != null && progressDialog.isShowing())
                Utils.dismissProgressDialog();
            if (Utils.isValidString(Globals.lastErrMsg)) {
                showAlert(Globals.lastErrMsg);
            } else {
                showAlert(getString(R.string.success_transaction));
                if (Utils.getConnectivityStatus(mContext)) {
                    new GetBalanceAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    new GetTransactionsAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
            super.onPostExecute(o);
        }
    }

    class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
        private ArrayList<Transaction> list;

        public class ViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.tv_date_timestamp)
            TextView dateTimeTv;
            @BindView(R.id.tv_description)
            TextView descriptionTv;
            @BindView(R.id.tv_transaction_amount)
            TextView amountTv;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

        }

        public TransactionAdapter(ArrayList<Transaction> myDataset) {
            list = myDataset;
        }

        private void updateData(ArrayList<Transaction> dataList){
            list = dataList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_transaction, parent, false);
            ViewHolder ViewHolder = new ViewHolder(view);
            return ViewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Transaction data = list.get(position);
            holder.dateTimeTv.setText(getDateStr(data.getDate()));
            holder.amountTv.setText(data.getAmount() + data.getCurrency());
            holder.descriptionTv.setText(data.getDescription());
        }

        @Override
        public int getItemCount() {
            count = list.size();
            return list.size();
        }

    }

    private String getDateStr(String originalDateStr) {
        DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        DateFormat targetFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
        Date date = null;
        try {
            date = originalFormat.parse(originalDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String formattedDate = targetFormat.format(date);
        return formattedDate;
    }
}
