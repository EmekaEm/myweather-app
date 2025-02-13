package com.weatherapp.myweatherapp.controller;

import com.weatherapp.myweatherapp.model.CityInfo;
import com.weatherapp.myweatherapp.service.WeatherService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WeatherController {

    @Autowired
    WeatherService weatherService;

    /**
     * Retrieves a forecast for a given city.
     */
    @GetMapping("/forecast/{city}")
    public ResponseEntity<CityInfo> forecastByCity(@PathVariable("city") String city) {
        CityInfo ci = weatherService.forecastByCity(city);
        return ResponseEntity.ok(ci);
    }

    /**
     * Compares daylight hours between two cities.
     */
    @GetMapping("/compareDaylight")
    @ResponseBody
    public ResponseEntity<String> compareDaylight(
            @RequestParam String city1,
            @RequestParam String city2
    ) {
        try {
            CityInfo ci1 = weatherService.forecastByCity(city1);
            CityInfo ci2 = weatherService.forecastByCity(city2);

            // Extract sunrise/sunset times
            String sunrise1 = ci1.getCurrentConditions().getSunrise();
            String sunset1 = ci1.getCurrentConditions().getSunset();
            String sunrise2 = ci2.getCurrentConditions().getSunrise();
            String sunset2 = ci2.getCurrentConditions().getSunset();

            if (sunrise1 == null || sunset1 == null || sunrise2 == null || sunset2 == null) {
                return ResponseEntity.badRequest().body("Missing sunrise/sunset data for one or both cities.");
            }

            // Determine the correct time format dynamically
            DateTimeFormatter formatter = detectTimeFormat(sunrise1);

            // Parse times
            LocalTime sunriseTime1 = LocalTime.parse(sunrise1, formatter);
            LocalTime sunsetTime1 = LocalTime.parse(sunset1, formatter);
            LocalTime sunriseTime2 = LocalTime.parse(sunrise2, formatter);
            LocalTime sunsetTime2 = LocalTime.parse(sunset2, formatter);

            // Calculate daylight duration
            long dayLength1 = ChronoUnit.MINUTES.between(sunriseTime1, sunsetTime1);
            long dayLength2 = ChronoUnit.MINUTES.between(sunriseTime2, sunsetTime2);

            // Compare and build response
            String message;
            if (dayLength1 > dayLength2) {
                message = String.format("%s has a longer day with %d minutes (vs %d minutes in %s).",
                        city1, dayLength1, dayLength2, city2);
            } else if (dayLength2 > dayLength1) {
                message = String.format("%s has a longer day with %d minutes (vs %d minutes in %s).",
                        city2, dayLength2, dayLength1, city1);
            } else {
                message = String.format("Both %s and %s have the same daylight duration: %d minutes.",
                        city1, city2, dayLength1);
            }

            return ResponseEntity.ok(message);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error comparing daylight hours: " + e.getMessage());
        }
    }

    /**
     * Checks which city is currently experiencing rain.
     */
    @GetMapping("/rainCheck")
    @ResponseBody
    public ResponseEntity<String> rainCheck(
            @RequestParam String city1,
            @RequestParam String city2
    ) {
        try {
            CityInfo ci1 = weatherService.forecastByCity(city1);
            CityInfo ci2 = weatherService.forecastByCity(city2);

            // Access 'preciptype' inside 'currentConditions'
            Object precip1 = ci1.getCurrentConditions().getPreciptype();
            Object precip2 = ci2.getCurrentConditions().getPreciptype();

            boolean isRainingCity1 = isRaining(precip1);
            boolean isRainingCity2 = isRaining(precip2);

            String message;
            if (isRainingCity1 && isRainingCity2) {
                message = String.format("It is currently raining in both %s and %s.", city1, city2);
            } else if (isRainingCity1) {
                message = String.format("It is currently raining in %s.", city1);
            } else if (isRainingCity2) {
                message = String.format("It is currently raining in %s.", city2);
            } else {
                message = "It is not currently raining in either city.";
            }

            return ResponseEntity.ok(message);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error checking rain status: " + e.getMessage());
        }
    }

    /**
     * Determines if precipitation data indicates rain.
     */
    private boolean isRaining(Object precip) {
        if (precip == null) {
            return false;
        }
        // If 'preciptype' is an array, check if it contains "rain"
        if (precip instanceof Iterable) {
            for (Object item : (Iterable<?>) precip) {
                if ("rain".equalsIgnoreCase(item.toString())) {
                    return true;
                }
            }
            return false;
        }
        // If it's a single string
        return "rain".equalsIgnoreCase(precip.toString());
    }

    /**
     * Detects the time format and returns the corresponding DateTimeFormatter.
     */
    private DateTimeFormatter detectTimeFormat(String timeString) {
        if (Pattern.matches("\\d{2}:\\d{2}:\\d{2}", timeString)) {  // HH:mm:ss format
            return DateTimeFormatter.ofPattern("HH:mm:ss");
        } else if (Pattern.matches("\\d{1,2}:\\d{2} [APap][Mm]", timeString)) {  // hh:mm a format (e.g., "7:19 AM")
            return DateTimeFormatter.ofPattern("hh:mm a");
        } else {  // Default to HH:mm format
            return DateTimeFormatter.ofPattern("HH:mm");
        }
    }
}