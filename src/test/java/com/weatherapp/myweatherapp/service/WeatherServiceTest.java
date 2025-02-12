package com.weatherapp.myweatherapp.service;

import com.weatherapp.myweatherapp.model.CityInfo;
import com.weatherapp.myweatherapp.model.CityInfo.CurrentConditions;
import com.weatherapp.myweatherapp.repository.VisualcrossingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private VisualcrossingRepository weatherRepo; // Mocking the repository

    @InjectMocks
    private WeatherService weatherService; // Service under test

    private CityInfo city1;
    private CityInfo city2;

    @BeforeEach
    void setUp() {
        // Mock City 1 (e.g., London)
        city1 = new CityInfo();
        CurrentConditions conditions1 = new CurrentConditions();
        conditions1.setSunrise("06:00:00");  // Using HH:mm:ss format for consistency
        conditions1.setSunset("18:00:00");  // 6:00 PM
        conditions1.setPreciptype("rain"); // City 1 is raining
        city1.setCurrentConditions(conditions1);

        // Mock City 2 (e.g., Paris)
        city2 = new CityInfo();
        CurrentConditions conditions2 = new CurrentConditions();
        conditions2.setSunrise("07:00:00");
        conditions2.setSunset("19:30:00");
        conditions2.setPreciptype(null); // City 2 is not raining
        city2.setCurrentConditions(conditions2);

        // Use lenient() to prevent UnnecessaryStubbingException
        lenient().when(weatherRepo.getByCity("London")).thenReturn(city1);
        lenient().when(weatherRepo.getByCity("Paris")).thenReturn(city2);
    }

    /**
     * Test that forecastByCity() fetches and returns weather data correctly.
     */
    @Test
    void testForecastByCity() {
        when(weatherRepo.getByCity("London")).thenReturn(city1);  // Stubbing only for this test
        CityInfo result = weatherService.forecastByCity("London");

        assertNotNull(result);
        assertEquals("06:00:00", result.getCurrentConditions().getSunrise());
        assertEquals("rain", result.getCurrentConditions().getPreciptype());
    }

    /**
     * Test exception handling when API call fails.
     */
    @Test
    void testForecastByCity_ExceptionHandling() {
        when(weatherRepo.getByCity(anyString())).thenThrow(new RuntimeException("API error"));

        assertThrows(RuntimeException.class, () -> weatherService.forecastByCity("London"));
    }
}