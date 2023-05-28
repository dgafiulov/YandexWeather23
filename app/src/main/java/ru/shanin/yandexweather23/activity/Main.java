package ru.shanin.yandexweather23.activity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.mapview.MapView;

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
    private TextView temperatureTV;
    private APIServiceYandexWeather service;
    private City city;
    private MapView mapView;
    InputListener inputListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitFactory.setApiKey("your-mapkit-api-key");
        setContentView(R.layout.activity_main);
        createService(56, 17);
        initView();
        loadData();
        setInputListener();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    private void initView() {
        textView = findViewById(R.id.tw_weather);
        temperatureTV = findViewById(R.id.temperature);
        MapKitFactory.initialize(this);
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.getMap().move(
                new CameraPosition(new Point(55.751574, 37.573856), 11.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 0),
                null);
    }

    private void createService(double lat, double lon) {
        service = APIServiceConstructor.CreateService(
                APIServiceYandexWeather.class,
                APIConfigYandexWeather.HOST_URL);
        city = new City(lat, lon);     //Ekb
        //city = new City( 55.74, 37.62);     //Msc
    }

    private void loadData() {
        AsyncTask.execute(() -> {
            Call<ResponseData> call_get = service.getGetCityWeather(
                    city.getLat(), city.getLon()
            );
            call_get.enqueue(new Callback<ResponseData>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(
                        @NonNull Call<ResponseData> call,
                        @NonNull Response<ResponseData> response
                ) {
                    if (response.body() != null) {
                        String weather = getResponse(response.body().toString()).fact.condition;
                        textView.setText(weather + getCorrectImage(weather));
                        double temperature = getResponse(response.body().toString()).fact.temp;
                        temperatureTV.setText(String.valueOf(temperature));
                    }
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
                }
            });
        });
    }

    private ResponseData getResponse(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, ResponseData.class);
    }

    private void setInputListener() {
        inputListener = new InputListener() {
            @Override
            public void onMapTap(@NonNull Map map, @NonNull Point point) {
                updateData(point.getLatitude(), point.getLongitude());
            }

            @Override
            public void onMapLongTap(@NonNull Map map, @NonNull Point point) {
                updateData(point.getLatitude(), point.getLongitude());
            }
        };
        mapView.getMap().addInputListener(inputListener);
    }

    private void updateData(double lat, double lon) {
        city.setLat(lat);
        city.setLon(lon);
        loadData();
    }


    private String getCorrectImage(String weather) {
        switch (weather) {
            case "clear":
                return "☀";
            case "partly-cloudy":
                return "⛅";
            case "cloudy":
                return "🌥";
            case "overcast":
                return "☁";
            case "drizzle":
            case "light-rain":
            case "rain":
            case "moderate-rain":
                return "🌧";
            case "heavy-rain":
            case "continuous-heavy-rain":
            case "showers":
            case "thunderstorm":
            case "thunderstorm-with-rain":
            case "thunderstorm-with-hail":
                return "⛈";
            case "wet-snow":
            case "light-snow":
            case "snow":
            case "snow-showers":
            case "hail":
                return "🌨";
        }
        return "🤨";
    }
}