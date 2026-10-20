package com.tnsfrp.document;

/** POSTED -> ACCEPTED -> PICKED_UP -> COMPLETED. An unaccepted listing past its expiry becomes EXPIRED. */
public enum ListingStatus { POSTED, ACCEPTED, PICKED_UP, COMPLETED, EXPIRED }
