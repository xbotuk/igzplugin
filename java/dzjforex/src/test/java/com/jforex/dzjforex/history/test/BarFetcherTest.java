package com.jforex.dzjforex.history.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.Filter;
import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.dzjforex.config.Constant;
import com.jforex.dzjforex.history.BarFetchTimeCalculator;
import com.jforex.dzjforex.history.BarFetchTimes;
import com.jforex.dzjforex.history.BarFetcher;
import com.jforex.dzjforex.history.HistoryProvider;
import com.jforex.dzjforex.test.util.CommonUtilForTest;
import com.jforex.dzjforex.time.DateTimeUtils;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class BarFetcherTest extends CommonUtilForTest {

    private BarFetcher barFetcher;

    @Mock
    private HistoryProvider historyProviderMock;
    @Mock
    private BarFetchTimeCalculator barFetchTimeCalculatorMock;
    @Mock
    private IBar barAMock;
    @Mock
    private IBar barBMock;
    private final double barAOpen = 1.32123;
    private final double barBOpen = 1.32126;

    private final double barAClose = 1.32107;
    private final double barBClose = 1.32111;

    private final double barAHigh = 1.32123;
    private final double barBHigh = 1.32126;

    private final double barALow = 1.32123;
    private final double barBLow = 1.32126;

    private final long barATime = 12L;
    private final long barBTime = 14L;

    private final double barAVol = 223.34;
    private final double barBVol = 250.45;

    private final Instrument fetchInstrument = Instrument.EURUSD;
    private final Period period = Period.ONE_MIN;
    private final OfferSide offerSide = OfferSide.ASK;
    private final Filter filter = Filter.WEEKENDS;
    private final double startDate = 12.4;
    private final double endDate = 13.7;
    private final int tickMinutes = 1;
    private final int nTicks = 270;
    private double tickParams[];
    private final List<IBar> barMockList = new ArrayList<>();
    private final long calculatedBarStartTime = 11L;
    private final long calculatedBarEndTime = 17L;
    private final BarFetchTimes barFetchTimes = new BarFetchTimes(calculatedBarStartTime, calculatedBarEndTime);

    @Before
    public void setUp() {
        setBarExpectations(barAMock,
                           barAOpen,
                           barAClose,
                           barAHigh,
                           barALow,
                           barATime,
                           barAVol);
        setBarExpectations(barBMock,
                           barBOpen,
                           barBClose,
                           barBHigh,
                           barBLow,
                           barBTime,
                           barBVol);

        barMockList.add(barAMock);
        barMockList.add(barBMock);
        tickParams = new double[barMockList.size() * 7];
        when(barFetchTimeCalculatorMock.calculate(DateTimeUtils.getMillisFromOLEDate(endDate),
                                                  nTicks,
                                                  period))
                                                      .thenReturn(barFetchTimes);

        barFetcher = new BarFetcher(historyProviderMock, barFetchTimeCalculatorMock);
    }

    private void setBarExpectations(final IBar barMock,
                                    final double open,
                                    final double close,
                                    final double high,
                                    final double low,
                                    final long time,
                                    final double volume) {
        when(barMock.getOpen()).thenReturn(open);
        when(barMock.getClose()).thenReturn(close);
        when(barMock.getHigh()).thenReturn(high);
        when(barMock.getLow()).thenReturn(low);
        when(barMock.getTime()).thenReturn(time);
        when(barMock.getVolume()).thenReturn(volume);
    }

    private int callFetch() {
        return barFetcher.fetch(fetchInstrument,
                                startDate,
                                endDate,
                                tickMinutes,
                                nTicks,
                                tickParams);
    }

    private void setReturnedTickList(final List<IBar> barList) {
        when(historyProviderMock.fetchBars(fetchInstrument,
                                           period,
                                           offerSide,
                                           filter,
                                           calculatedBarStartTime,
                                           calculatedBarEndTime))
                                               .thenReturn(barList);
    }

    @Test
    public void whenHistoryProviderReturnesEmptyListTheResultIsHistoryUnavailable() {
        setReturnedTickList(new ArrayList<>());

        assertThat(callFetch(), equalTo(Constant.HISTORY_UNAVAILABLE));
    }

    public class WhenHistoryProviderReturnsBarMockList {

        private int returnValue;

        @Before
        public void setUp() {
            setReturnedTickList(barMockList);

            returnValue = callFetch();
        }

        @Test
        public void returnedTickSizeIsTwo() {
            assertThat(returnValue, equalTo(2));
        }

        @Test
        public void fetchWasCalledCorrectOnHistoryProvider() {
            verify(historyProviderMock).fetchBars(fetchInstrument,
                                                  period,
                                                  offerSide,
                                                  filter,
                                                  calculatedBarStartTime,
                                                  calculatedBarEndTime);
        }

        @Test
        public void filledBarValuesAreCorrect() {
            assertThat(tickParams[0], equalTo(barBOpen));
            assertThat(tickParams[1], equalTo(barBClose));
            assertThat(tickParams[2], equalTo(barBHigh));
            assertThat(tickParams[3], equalTo(barBLow));
            assertThat(tickParams[4], equalTo(DateTimeUtils.getUTCTimeFromBar(barBMock)));
            assertThat(tickParams[5], equalTo(0.0));
            assertThat(tickParams[6], equalTo(barBVol));

            assertThat(tickParams[7], equalTo(barAOpen));
            assertThat(tickParams[8], equalTo(barAClose));
            assertThat(tickParams[9], equalTo(barAHigh));
            assertThat(tickParams[10], equalTo(barALow));
            assertThat(tickParams[11], equalTo(DateTimeUtils.getUTCTimeFromBar(barAMock)));
            assertThat(tickParams[12], equalTo(0.0));
            assertThat(tickParams[13], equalTo(barAVol));
        }
    }
}
