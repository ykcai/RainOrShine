package me.michaelcai.rainorshine.app;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * http://api.openweathermap.org/data/2.5/weather?lat=-79.42&lon=43.7&cnt=7&mode=JSON&units=metric&appid=6d33e2549400ffb5027156e75c3fbee3
 * <p/>
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            //Toast.makeText(getContext(), "REFRESH",Toast.LENGTH_SHORT).show();
            new GetNetworkDataTask().execute("43.65", "-79.38");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] forecastArray = {"Today - Sunny 2/12",
                "Tomorrow - Sunny 2/12",
                "Saturday - Sunny 2/12",
                "Sunday - Sunny 2/12",
                "Monday - Sunny 2/12"};
        List<String> weekForecast = new ArrayList<>(Arrays.asList(forecastArray));
        ArrayAdapter<String> forecastAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast,
                R.id.list_item_forecast_textview, weekForecast);

        ListView forecastList = (ListView) rootView.findViewById(R.id.listview_forecast);
        forecastList.setAdapter(forecastAdapter);

        return rootView;
    }

    public class GetNetworkDataTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = GetNetworkDataTask.class.getSimpleName();

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
        }

        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonString = null;
            final int numDays = 7;
            final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM_LAT = "lat";
            final String QUERY_PARAM_LONG = "lon";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APPID_PARAM = "APPID";

            try {
                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?lat=-79.42&lon=43.7&%22%20+%20//%20%22cnt=7&mode=JSON&units=metric&appid=6d33e2549400ffb5027156e75c3fbee3");
                //String baseUrl = "http://api.openweathermap.org/data/2.5/weather?lat=-79.42&lon=43.7&cnt=7&mode=JSON&units=metric";
                //String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM_LAT, params[0])
                        .appendQueryParameter(QUERY_PARAM_LONG, params[0])
                        .appendQueryParameter(FORMAT_PARAM, "JSON")
                        .appendQueryParameter(UNITS_PARAM, "metric")
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();
                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                forecastJsonString = buffer.toString();

                Log.v(LOG_TAG, "Forecast string: " + forecastJsonString);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);

                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ActivityFragment", "Error closing stream", e);

                    }
                }
            }

            try {
                return getWeaterDataFromJson(forecastJsonString, numDays);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();

            }
            return null;
        }

        private String getParsedDateString(long time) {
            SimpleDateFormat date = new SimpleDateFormat("EEE MMM dd");
            return date.format(time);
        }

        private String formatMaxMin(double high, double low) {
            long approxhigh = Math.round(high);
            long approxlow = Math.round((low));

            String maxLow = approxhigh + "/" + approxlow;
            return maxLow;
        }

        private String[] getWeaterDataFromJson(String weatherJsonStr, int numDays)
                throws JSONException {
            final String JSONList;
            JSONList = "list";
            final String JSONWeather;
            JSONWeather = "list";
            final String JSONTemperature;
            JSONTemperature = "list";
            final String JSONMax;
            JSONMax = "list";
            final String JSONMin;
            JSONMin = "list";
            final String JSONDescription;
            JSONDescription = "list";

            JSONObject forecastJson = new JSONObject(weatherJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(JSONList);

            Time dayTime = new Time();
            dayTime.setToNow();

            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {
                String day;
                String description;
                String highAndLow;

                JSONObject dayForecast = weatherArray.getJSONObject(i);

                long dateTime;

                dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getParsedDateString(dateTime);

                JSONObject weatherObject = dayForecast.getJSONArray(JSONWeather).getJSONObject(0);
                description = weatherObject.getString(JSONDescription);

                JSONObject temperatureObject = dayForecast.getJSONObject(JSONTemperature);
                double high = temperatureObject.getDouble(JSONMax);
                double low = temperatureObject.getDouble(JSONMin);

            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }
    }
}
