package com.pmi.tpd.spring.convert;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assumptions.assumingThat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;

import com.pmi.tpd.testing.junit5.TestCase;

/**
 * Tests for {@link InetAddressFormatter}.
 *
 * @author Phillip Webb
 */
class InetAddressFormatterTest extends TestCase {

    @ConversionServiceTest
    void convertFromInetAddressToStringShouldConvert(final ConversionService conversionService)
            throws UnknownHostException {
        assumingThat(isResolvable("example.com"), () -> {
            final InetAddress address = InetAddress.getByName("example.com");
            final String converted = conversionService.convert(address, String.class);
            assertThat(converted, equalTo(address.getHostAddress()));
        });
    }

    @ConversionServiceTest
    void convertFromStringToInetAddressShouldConvert(final ConversionService conversionService) {
        assumingThat(isResolvable("example.com"), () -> {
            final InetAddress converted = conversionService.convert("example.com", InetAddress.class);
            assertThat(converted.toString(), Matchers.startsWith("example.com"));
        });
    }

    @ConversionServiceTest
    void convertFromStringToInetAddressWhenHostDoesNotExistShouldThrowException(
        final ConversionService conversionService) {
        final String missingDomain = "ireallydontexist.example.com";
        assumingThat(!isResolvable("ireallydontexist.example.com"),
            () -> assertThrows(ConversionFailedException.class,
                () -> conversionService.convert(missingDomain, InetAddress.class)));
    }

    private boolean isResolvable(final String host) {
        try {
            InetAddress.getByName(host);
            return true;
        } catch (final UnknownHostException ex) {
            return false;
        }
    }

    static Stream<? extends Arguments> conversionServices() {
        return ConversionServiceArguments.with(new InetAddressFormatter());
    }

}