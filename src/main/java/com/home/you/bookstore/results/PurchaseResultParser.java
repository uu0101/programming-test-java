package com.home.you.bookstore.results;

import com.home.you.bookstore.Constants;
import com.home.you.bookstore.utils.ParseUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.home.you.bookstore.Constants.*;

public class PurchaseResultParser {
	
	private PurchaseResultParser() {};
    public static PurchaseResult decode(InputStream stream) throws IOException {
        final PurchaseResult.Builder builder = PurchaseResult.builder();
        try {
            final BufferedReader input = new BufferedReader(new InputStreamReader(stream));
            extractTotalPriceFromFirstLinePopulateBuilder(input.readLine(), builder);
            extractStatusesFromSecondLineAndPopulateBuilder(input.readLine(), builder);
            input.close();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read data from stream.", e);
        }
        final PurchaseResult result = builder.build();
        return result;
    }

    public static byte[] encode(PurchaseResult result) throws IOException {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(result.getTotalPrice());
        messageBuilder.append(NEW_LINE);
        for (Status status : result.getStatuses()) {
            messageBuilder.append(status.value());
            messageBuilder.append(SEMICOLON);
        }
        final String message = messageBuilder.toString();
        final byte[] blob = message.getBytes(UTF8);
        return blob;
    }

    private static void extractTotalPriceFromFirstLinePopulateBuilder(String line, PurchaseResult.Builder builder) {
        final BigDecimal value = ParseUtils.parseBigDecimal(line);
        builder.withTotalPrice(value);
    }

    private static void extractStatusesFromSecondLineAndPopulateBuilder(String line, PurchaseResult.Builder builder) {
        final List<Status> statusList = new ArrayList<>();
        if(line != null) {
            final String[] values = line.split(Constants.SEMICOLON);
            for (String value : values) {
                final int v = Integer.parseInt(value);
                Status status = Status.valueOf(v);
                statusList.add(status);
            }
        }
        final Statuses statueses = Statuses.of(statusList);
        builder.withStatuses(statueses);
    }
}
