package com.snowresorts.activity.domain.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.snowresorts.activity.domain.model.RunMetrics;
import com.snowresorts.activity.domain.model.TrackPoint;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("MetricsCalculator")
class MetricsCalculatorTest {

    private static final Instant T0 = Instant.parse("2026-01-15T09:00:00Z");

    private MetricsCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new MetricsCalculator();
    }

    /** A clean 3-point straight descent: ~111.2 m per 0.001° latitude step, dropping 10 m each. */
    private static List<TrackPoint> cleanDescent() {
        List<TrackPoint> points = new ArrayList<>();
        points.add(new TrackPoint(T0, 45.000, 6.000, 2000.0, null));
        points.add(new TrackPoint(T0.plusSeconds(10), 45.001, 6.000, 1990.0, 40.0));
        points.add(new TrackPoint(T0.plusSeconds(20), 45.002, 6.000, 1980.0, 50.0));
        return points;
    }

    @Test
    @DisplayName("calculate_knownTrack_returnsExpectedMetrics")
    void calculate_knownTrack_returnsExpectedMetrics() {
        // Act
        RunMetrics metrics = calculator.calculate(cleanDescent());

        // Assert: two ~111.2 m segments -> ~222.4 m total (Haversine)
        assertThat(metrics.distanceM()).isCloseTo(222.39, within(1.0));
        assertThat(metrics.durationSec()).isEqualTo(20L);
        assertThat(metrics.maxSpeedKmh()).isEqualTo(50.0); // max of reported / segment speed
        assertThat(metrics.avgSpeedKmh()).isCloseTo(40.03, within(1.0));
        assertThat(metrics.verticalDropM()).isCloseTo(20.0, within(0.001));
        assertThat(metrics.maxAltitudeM()).isCloseTo(2000.0, within(0.001));
        assertThat(metrics.maxInclinationDeg()).isCloseTo(5.14, within(0.2));
        assertThat(metrics.avgInclinationDeg()).isCloseTo(5.14, within(0.2));
    }

    @Test
    @DisplayName("calculate_implausibleReportedSpeed_ignoresItAndUsesSegment")
    void calculate_implausibleReportedSpeed_ignoresItAndUsesSegment() {
        List<TrackPoint> points = List.of(
                new TrackPoint(T0, 45.000, 6.000, 2000.0, null),
                new TrackPoint(T0.plusSeconds(10), 45.001, 6.000, 1990.0, 280.0));

        RunMetrics metrics = calculator.calculate(points);

        // 280 km/h chip reading is discarded; segment-derived ~40 km/h wins.
        assertThat(metrics.maxSpeedKmh()).isCloseTo(40.03, within(1.0));
    }

    @Test
    @DisplayName("calculate_noReportedSpeeds_derivesMaxSpeedFromSegments")
    void calculate_noReportedSpeeds_derivesMaxSpeedFromSegments() {
        List<TrackPoint> points = List.of(
                new TrackPoint(T0, 45.000, 6.000, 2000.0, null),
                new TrackPoint(T0.plusSeconds(10), 45.001, 6.000, 1990.0, null));

        RunMetrics metrics = calculator.calculate(points);

        // ~111.2 m over 10 s = ~11.12 m/s = ~40.03 km/h
        assertThat(metrics.maxSpeedKmh()).isCloseTo(40.03, within(1.0));
    }

    @Test
    @DisplayName("calculate_emptyList_returnsAllZeros")
    void calculate_emptyList_returnsAllZeros() {
        assertThat(calculator.calculate(List.of())).isEqualTo(RunMetrics.zero());
    }

    @Test
    @DisplayName("calculate_singlePoint_returnsAllZeros")
    void calculate_singlePoint_returnsAllZeros() {
        List<TrackPoint> single = List.of(new TrackPoint(T0, 45.0, 6.0, 2000.0, 12.0));

        assertThat(calculator.calculate(single)).isEqualTo(RunMetrics.zero());
    }

    static Stream<Arguments> spikePoints() {
        return Stream.of(
                // 2 km jump from the last point (>500 m) one second later
                Arguments.of("2km jump",
                        new TrackPoint(T0.plusSeconds(21), 45.020, 6.000, 1979.0, 300.0)),
                // ~167 m in 0.1 s -> ~6000 km/h implied speed (>150) within the jump threshold
                Arguments.of("over-speed",
                        new TrackPoint(T0.plusMillis(20_100), 45.0035, 6.000, 1979.0, 300.0)));
    }

    @ParameterizedTest(name = "{0} spike is rejected")
    @MethodSource("spikePoints")
    @DisplayName("clean_spikePoint_excludesItFromTrack")
    void clean_spikePoint_excludesItFromTrack(String description, TrackPoint spike) {
        List<TrackPoint> withSpike = new ArrayList<>(cleanDescent());
        withSpike.add(spike);

        List<TrackPoint> cleaned = calculator.clean(withSpike);

        assertThat(cleaned).hasSize(3).doesNotContain(spike);
    }

    @ParameterizedTest(name = "{0} spike does not affect distance/maxSpeed")
    @MethodSource("spikePoints")
    @DisplayName("calculate_spikePoint_doesNotAffectDistanceOrMaxSpeed")
    void calculate_spikePoint_doesNotAffectDistanceOrMaxSpeed(String description, TrackPoint spike) {
        List<TrackPoint> withSpike = new ArrayList<>(cleanDescent());
        withSpike.add(spike);

        RunMetrics metrics = calculator.calculate(withSpike);

        // Identical to the clean track: the 300 km/h / 2 km spike is filtered out.
        assertThat(metrics.distanceM()).isCloseTo(222.39, within(1.0));
        assertThat(metrics.maxSpeedKmh()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("calculate_uphillTrack_usesMaxMinusMinVerticalRange")
    void calculate_uphillTrack_usesMaxMinusMinVerticalRange() {
        List<TrackPoint> uphill = List.of(
                new TrackPoint(T0, 45.000, 6.000, 1980.0, 10.0),
                new TrackPoint(T0.plusSeconds(10), 45.001, 6.000, 2000.0, 10.0));

        RunMetrics metrics = calculator.calculate(uphill);

        assertThat(metrics.verticalDropM()).isCloseTo(20.0, within(0.001));
        assertThat(metrics.maxAltitudeM()).isCloseTo(2000.0, within(0.001));
    }
}
