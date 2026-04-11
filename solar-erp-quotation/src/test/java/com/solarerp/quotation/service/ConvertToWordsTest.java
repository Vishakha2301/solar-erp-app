package com.solarerp.quotation.service;

import com.solarerp.costing.repository.CostingRepository;
import com.solarerp.costing.service.CostingService;
import com.solarerp.quotation.repository.QuotationRepository;
import com.solarerp.quotation.service.impl.QuotationDocumentServiceImpl;
import com.solarerp.quotation.utility.AmountToWordsConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConvertToWords Tests")
class ConvertToWordsTest {

    @Mock
    private QuotationRepository quotationRepository;

    @Mock
    private CostingRepository costingRepository;

    @Mock
    private CostingService costingService;

    @Mock
    private AmountToWordsConverter converter;

    private QuotationDocumentServiceImpl documentService;


    @BeforeEach
    void setUp() {
        documentService = new QuotationDocumentServiceImpl(
                quotationRepository,
                costingRepository,
                costingService,
                converter);
    }

    private String convertToWords(long number) {
        return (String) ReflectionTestUtils.invokeMethod(
                documentService, "convertToWords", number);
    }

    private String amountInWords(BigDecimal amount) {
        return (String) ReflectionTestUtils.invokeMethod(
                documentService, "amountInWords", amount);
    }

    @Nested
    @DisplayName("Zero")
    class ZeroTests {

        @Test
        @DisplayName("Zero returns 'Zero'")
        void zero_returnsZero() {
            assertThat(convertToWords(0)).isEqualTo("Zero");
        }
    }

    @Nested
    @DisplayName("Ones (1-19)")
    class OnesTests {

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "1, One",
                "2, Two",
                "3, Three",
                "4, Four",
                "5, Five",
                "6, Six",
                "7, Seven",
                "8, Eight",
                "9, Nine",
                "10, Ten",
                "11, Eleven",
                "12, Twelve",
                "13, Thirteen",
                "14, Fourteen",
                "15, Fifteen",
                "16, Sixteen",
                "17, Seventeen",
                "18, Eighteen",
                "19, Nineteen"
        })
        void ones_returnsCorrectWord(long number, String expected) {
            assertThat(convertToWords(number)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Tens (20-99)")
    class TensTests {

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "20, Twenty",
                "21, Twenty One",
                "30, Thirty",
                "35, Thirty Five",
                "40, Forty",
                "42, Forty Two",
                "50, Fifty",
                "56, Fifty Six",
                "60, Sixty",
                "67, Sixty Seven",
                "70, Seventy",
                "78, Seventy Eight",
                "80, Eighty",
                "89, Eighty Nine",
                "90, Ninety",
                "99, Ninety Nine"
        })
        void tens_returnsCorrectWord(long number, String expected) {
            assertThat(convertToWords(number)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Hundreds (100-999)")
    class HundredsTests {

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "100, One Hundred",
                "101, One Hundred One",
                "110, One Hundred Ten",
                "115, One Hundred Fifteen",
                "200, Two Hundred",
                "250, Two Hundred Fifty",
                "500, Five Hundred",
                "555, Five Hundred Fifty Five",
                "900, Nine Hundred",
                "999, Nine Hundred Ninety Nine"
        })
        void hundreds_returnsCorrectWord(long number, String expected) {
            assertThat(convertToWords(number)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Thousands (1000-99999)")
    class ThousandsTests {

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "1000, One Thousand",
                "1001, One Thousand One",
                "1500, One Thousand Five Hundred",
                "5000, Five Thousand",
                "10000, Ten Thousand",
                "15000, Fifteen Thousand",
                "20000, Twenty Thousand",
                "50000, Fifty Thousand",
                "99999, Ninety Nine Thousand Nine Hundred Ninety Nine"
        })
        void thousands_returnsCorrectWord(long number, String expected) {
            assertThat(convertToWords(number)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Lakhs (100000-9999999)")
    class LakhsTests {

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "100000, One Lakh",
                "100001, One Lakh One",
                "150000, One Lakh Fifty Thousand",
                "200000, Two Lakh",
                "500000, Five Lakh",
                "1000000, Ten Lakh",
                "2500000, Twenty Five Lakh",
                "9999999, Ninety Nine Lakh Ninety Nine Thousand Nine Hundred Ninety Nine"
        })
        void lakhs_returnsCorrectWord(long number, String expected) {
            assertThat(convertToWords(number)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Crores (10000000+)")
    class CroresTests {

        @ParameterizedTest(name = "{0} = {1}")
        @CsvSource({
                "10000000, One Crore",
                "10000001, One Crore One",
                "20000000, Two Crore",
                "50000000, Five Crore",
                "100000000, Ten Crore",
                "150000000, Fifteen Crore",
                "250000000, Twenty Five Crore"
        })
        void crores_returnsCorrectWord(long number, String expected) {
            assertThat(convertToWords(number)).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("Real solar quotation amounts")
    class RealAmountTests {

        @Test
        @DisplayName("254500 — typical landed cost")
        void typicalLandedCost() {
            assertThat(convertToWords(254500))
                    .isEqualTo("Two Lakh Fifty Four Thousand Five Hundred");
        }

        @Test
        @DisplayName("337500 — typical grand total")
        void typicalGrandTotal() {
            assertThat(convertToWords(337500))
                    .isEqualTo("Three Lakh Thirty Seven Thousand Five Hundred");
        }

        @Test
        @DisplayName("78000 — typical subsidy amount")
        void typicalSubsidyAmount() {
            assertThat(convertToWords(78000))
                    .isEqualTo("Seventy Eight Thousand");
        }

        @Test
        @DisplayName("1500000 — larger system cost")
        void largerSystemCost() {
            assertThat(convertToWords(1500000))
                    .isEqualTo("Fifteen Lakh");
        }
    }

    @Nested
    @DisplayName("amountInWords()")
    class AmountInWordsTests {

        @Test
        @DisplayName("Wraps in Rupees ... Only")
        void amountInWords_wrapsCorrectly() {
            assertThat(amountInWords(BigDecimal.valueOf(254500)))
                    .isEqualTo(
                            "Rupees Two Lakh Fifty Four Thousand Five Hundred Only");
        }

        @Test
        @DisplayName("Returns empty string for null amount")
        void amountInWords_nullAmount_returnsEmpty() {
            assertThat(amountInWords(null)).isEmpty();
        }

        @Test
        @DisplayName("Handles zero amount")
        void amountInWords_zeroAmount() {
            assertThat(amountInWords(BigDecimal.ZERO))
                    .isEqualTo("Rupees Zero Only");
        }

        @Test
        @DisplayName("Truncates decimal part")
        void amountInWords_decimalAmount_truncatesDecimals() {
            assertThat(amountInWords(BigDecimal.valueOf(254500.75)))
                    .isEqualTo(
                            "Rupees Two Lakh Fifty Four Thousand Five Hundred Only");
        }
    }
}
