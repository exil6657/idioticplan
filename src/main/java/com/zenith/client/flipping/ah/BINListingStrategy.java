package com.zenith.client.flipping.ah;

import com.zenith.client.flipping.order.Order;
import com.zenith.client.flipping.order.RelistStrategy;

/** Thin wrapper around {@link RelistStrategy} for the AH subpackage. */
public final class BINListingStrategy {

    private BINListingStrategy() {}

    public static long priceFor(Order o) { return RelistStrategy.getInstance().computeListPrice(o); }
}
