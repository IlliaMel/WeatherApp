package com.example.weatherapp;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {

    Context context;
    List<Day> weatherList;
    View lastSelected;
    private OnItemClicked mListener;

    public WeatherAdapter(Context context, List<Day> weatherList , OnItemClicked mListener) {
        this.context = context;
        this.weatherList = weatherList;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.weather_day_item, parent, false);
        return new WeatherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final WeatherViewHolder holder, int position) {

        holder.weatherImage.setImageResource(weatherList.get(position).getImageUrl());
        holder.day.setText( new SimpleDateFormat("dd/MM").format(weatherList.get(position).getDay()));
        holder.weatherDescription.setText(weatherList.get(position).getWeatherDescription());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = holder.getAdapterPosition();
                if(lastSelected != null)
                    ((ConstraintLayout) lastSelected.findViewById(R.id.constraintlayout_weather_item)).setBackgroundColor(context.getResources().getColor(R.color.Background));
                ((ConstraintLayout) holder.itemView.findViewById(R.id.constraintlayout_weather_item)).setBackgroundColor(context.getResources().getColor(R.color.white));

                mListener.OnItemClicked(weatherList.get(pos));
                lastSelected = holder.itemView;


                //Intent intent = new Intent(context, .class);
                // putExtra("WEATHER_DESCRIPTION", weatherList.get(pos));
                //context.startActivity(intent);
            }
        });

    }

    public interface OnItemClicked {
        void OnItemClicked(Day day);
    }

    @Override
    public int getItemCount() {
        return weatherList.size();
    }


    public static final class WeatherViewHolder extends RecyclerView.ViewHolder{
        ImageView weatherImage;
        TextView day, weatherDescription;

        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            weatherImage = itemView.findViewById(R.id.weatherImage);
            day = itemView.findViewById(R.id.day);
            weatherDescription = itemView.findViewById(R.id.weatherDescription);
        }
    }

}
