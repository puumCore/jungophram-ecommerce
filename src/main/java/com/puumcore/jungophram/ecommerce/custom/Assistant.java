package com.puumcore.jungophram.ecommerce.custom;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.puumcore.jungophram.ecommerce.models.constants.Channel;
import com.puumcore.jungophram.ecommerce.models.objects.GenericRequest;
import com.puumcore.jungophram.ecommerce.models.objects.GenericResponse;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.val;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/18/2024 2:28 PM
 */

public abstract class Assistant {

    public static final Clock clock = Clock.system(ZoneId.of("Africa/Nairobi"));
    private static final String DATE_TIME_FORMAT = "yyyy-MMM-dd HH:mm:ss";
    private static final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Africa/Nairobi")));
    public static final String ERROR_SUFFIX = " Please try again later.";

    public static double roundOffToTheNearestDecimal(final String decimalPattern, final double param) {
        DecimalFormat df = new DecimalFormat(decimalPattern);
        df.setRoundingMode(RoundingMode.CEILING);
        String format = df.format(param);
        return Double.parseDouble(format);
    }

    public static int roundOffToNearestWholeNumber(final Long numerator, final int denominator) {
        final double result = (double) numerator / denominator;
        return (int) ((result % 1 != 0) ? Math.ceil(result) : result);
    }

    public static String generateToken(final Channel channel, final UUID session, final long expiryTimeInSeconds, final String secret) {
        val zoneOffset = clock.getZone().getRules().getOffset(LocalDateTime.now(clock));
        return JWT.create()
                .withSubject("E-commerce")
                .withAudience(session.toString())
                .withIssuedAt(LocalDateTime.now(clock).toInstant(zoneOffset))
                .withExpiresAt(LocalDateTime.now(clock).plusSeconds(expiryTimeInSeconds).toInstant(zoneOffset))
                .withIssuer("Jungopharm")
                .withClaim("channel", channel.name())
                .sign(Algorithm.HMAC256(secret));
    }

    protected final <B> GenericResponse<B> buildSuccessfulResponse(@NonNull GenericRequest.Header header, @NonNull String message) {
        final GenericResponse<B> genericResponse = new GenericResponse<>();
        genericResponse.setHeader(new GenericResponse.Header(getCurrentTimestamp(), header.requestId(), message));
        return genericResponse;
    }

    protected final <B> GenericResponse<B> buildSuccessfulResponse(@NonNull GenericRequest.Header header, @NonNull String message, @NonNull B b) {
        final GenericResponse<B> genericResponse = new GenericResponse<>();
        genericResponse.setHeader(new GenericResponse.Header(getCurrentTimestamp(), header.requestId(), message));
        genericResponse.setBody(b);
        return genericResponse;
    }


    protected final boolean containsSpecialCharacter(final String param) {
        if (param == null) {
            return true;
        }
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
        Matcher matcher = pattern.matcher(param);
        return matcher.find();
    }

    public static String getCurrentTimestamp() {
        return new SimpleDateFormat(DATE_TIME_FORMAT).format(calendar.getTime());
    }

    public static boolean correctHttpUrlFormat(@NotNull final String param) {
        return Pattern.matches("^(https?)://[^\\s/$.?#].\\S*$", param);
    }

    public static boolean correctUUIDFormat(@NotNull final String param) {
        return Pattern.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[4][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$", param);
    }

    protected boolean correctEmailFormat(@NotNull final String param) {
        return Pattern.matches("^[\\w!#$%&’*+/=?`{|}~^-]+(?:\\.[\\w!#$%&’*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$", param);
    }

}
