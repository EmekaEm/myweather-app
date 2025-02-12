package com.weatherapp.myweatherapp.service;

import com.weatherapp.myweatherapp.model.CityInfo;
import com.weatherapp.myweatherapp.repository.VisualcrossingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Service
public class WeatherService {

  @Autowired
  VisualcrossingRepository weatherRepo;

  public CityInfo forecastByCity(String city) {
    return weatherRepo.getByCity(city);
  }

  // Method to calculate daylight duration between sunrise and sunset
  public long calculateDaylightDuration(CityInfo cityInfo) {
      if (cityInfo == null || cityInfo.getCurrentConditions() == null) {
          return -1; // Return -1 if city info is invalid
      }

      String sunrise = cityInfo.getCurrentConditions().getSunrise();
      String sunset = cityInfo.getCurrentConditions().getSunset();

      if (sunrise == null || sunset == null) {
          return -1;
      }

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
      LocalTime sunriseTime = LocalTime.parse(sunrise, formatter);
      LocalTime sunsetTime = LocalTime.parse(sunset, formatter);

      return ChronoUnit.MINUTES.between(sunriseTime, sunsetTime);
  }

  // Method to check if it's raining in the given city
  public boolean isRaining(CityInfo cityInfo) {
      if (cityInfo == null || cityInfo.getCurrentConditions() == null) {
          return false;
      }

      Object precip = cityInfo.getCurrentConditions().getPreciptype();
      if (precip == null) {
          return false;
      }

      if (precip instanceof Iterable) {
          for (Object item : (Iterable<?>) precip) {
              if ("rain".equalsIgnoreCase(item.toString())) {
                  return true;
              }
          }
          return false;
      }
      return "rain".equalsIgnoreCase(precip.toString());
  }
}
