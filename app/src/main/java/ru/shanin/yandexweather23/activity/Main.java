package ru.shanin.yandexweather23.activity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.shanin.yandexweather23.R;
import ru.shanin.yandexweather23.api.APIServiceConstructor;
import ru.shanin.yandexweather23.api.config.APIConfigYandexWeather;
import ru.shanin.yandexweather23.api.config.APIServiceYandexWeather;
import ru.shanin.yandexweather23.data.City;
import ru.shanin.yandexweather23.data.responsedata.ResponseData;

public class Main extends AppCompatActivity {
    private TextView textView;
    private SwipeRefreshLayout refreshLayout;
    private APIServiceYandexWeather service;
    private City city;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createService();
        initView();
    }

    private void initView() {
        textView = findViewById(R.id.tw_weather);
        refreshLayout = findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this::loadData);
    }

    private void createService() {
        service = APIServiceConstructor.CreateService(
                APIServiceYandexWeather.class,
                APIConfigYandexWeather.HOST_URL);
        city = new City(56.50, 60.35);     //Ekb
        //city = new City( 55.74, 37.62);     //Msc
    }

    private void loadData() {
        refreshLayout.setRefreshing(true);
        AsyncTask.execute(() -> {
            Call<ResponseData> call_get = service.getGetCityWeather(
                    city.getLat(), city.getLon()
            );
            call_get.enqueue(new Callback<ResponseData>() {
                @Override
                public void onResponse(
                        @NonNull Call<ResponseData> call,
                        @NonNull Response<ResponseData> response
                ) {
                    if (response.body() != null) {
                        String text = (new Gson()).toJson(response.body());
                        textView.setText(text);
                        Toast.makeText(
                                getApplicationContext(),
                                text,
                                Toast.LENGTH_LONG
                        ).show();
                        Log.d("ResponseData", text);

                    }
                    refreshLayout.setRefreshing(false);
                }

                @Override
                public void onFailure(
                        @NonNull Call<ResponseData> call,
                        @NonNull Throwable t
                ) {
                    textView.setText(t.toString());
                    Toast.makeText(
                            getApplicationContext(),
                            t.toString(),
                            Toast.LENGTH_LONG
                    ).show();
                    Log.d("ResponseData", t.toString());
                    refreshLayout.setRefreshing(false);
                }
            });
        });
    }
}