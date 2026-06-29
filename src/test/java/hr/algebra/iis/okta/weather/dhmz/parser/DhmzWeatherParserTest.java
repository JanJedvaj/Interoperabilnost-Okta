package hr.algebra.iis.okta.weather.dhmz.parser;

import hr.algebra.iis.okta.weather.dhmz.dto.DhmzWeatherObservation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DhmzWeatherParserTest {

    private final DhmzWeatherParser parser = new DhmzWeatherParser();

    @Test
    void parsesDhmzCityObservations() {
        List<DhmzWeatherObservation> observations = parser.parse(sampleDhmzXml());

        assertEquals(2, observations.size());
        assertEquals("Zagreb-Maksimir", observations.getFirst().city());
        assertEquals("21.4", observations.getFirst().temperature());
        assertEquals("65", observations.getFirst().humidity());
        assertEquals("1015.2", observations.getFirst().pressure());
        assertEquals("Vedro", observations.getFirst().weather());
    }

    public static String sampleDhmzXml() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <Hrvatska>
                    <Grad>
                        <GradIme>Zagreb-Maksimir</GradIme>
                        <Podatci>
                            <Temp>21.4</Temp>
                            <Vlaga>65</Vlaga>
                            <Tlak>1015.2</Tlak>
                            <TlakTend>+</TlakTend>
                            <VjetarSmjer>NE</VjetarSmjer>
                            <VjetarBrzina>2.1</VjetarBrzina>
                            <Vrijeme>Vedro</Vrijeme>
                        </Podatci>
                    </Grad>
                    <Grad>
                        <GradIme>Split-Marjan</GradIme>
                        <Podatci>
                            <Temp>27.0</Temp>
                            <Vlaga>45</Vlaga>
                            <Tlak>1012.4</Tlak>
                            <TlakTend>-</TlakTend>
                            <VjetarSmjer>NW</VjetarSmjer>
                            <VjetarBrzina>3.3</VjetarBrzina>
                            <Vrijeme>Sunčano</Vrijeme>
                        </Podatci>
                    </Grad>
                </Hrvatska>
                """;
    }
}
