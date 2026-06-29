package hr.algebra.iis.okta.weather.dhmz.controller;

import hr.algebra.iis.okta.weather.dhmz.dto.DhmzWeatherObservation;
import hr.algebra.iis.okta.weather.dhmz.service.DhmzWeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/weather/dhmz")
public class DhmzWeatherController {

    private final DhmzWeatherService dhmzWeatherService;

    public DhmzWeatherController(DhmzWeatherService dhmzWeatherService) {
        this.dhmzWeatherService = dhmzWeatherService;
    }

    @GetMapping("/cities")
    public List<DhmzWeatherObservation> searchCities(@RequestParam(defaultValue = "") String query) {
        return dhmzWeatherService.searchCities(query);
    }
}
