package com.location.rader.utils;

public final class EndpointsConstants {

    private EndpointsConstants() {
        // Prevent instantiation
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    public static final String NEW_USER_REGISTER_ENDPOINT = "/register";

    public static final String LOGIN_ENDPOINT = "/login";

    public static final String LOCATION_UPDATE_ENDPOINT ="/location";

    public static final String USER_HISTORY_ENDPOINT ="/history";

    public static final String REQUEST_FOR_LOCATION_ACCESS_ENDPOINT = "/requestForLocationAccess";

    public static final String COORDINATES_OF_OTHER_USER_ENDPOINT = "/CoordinatesOfOtherUser";

    public static final String USER_HAVE_ACCESS_TO_ENDPOINT = "/userHaveAccessTo";

    public static final String NEW_NOTIFICATION_ENDPOINT = "/newNotification";

    public static final String GET_PENDING_NOTIFICATIONS_ENDPOINT = "/getPendingNotifications";

    public static final String ACCEPT_LOCATION_REQUEST_ENDPOINT = "/acceptRequest";

    public static final String LIST_OF_ALL_USERS_ENDPOINT = "/allUsers";

    public static final String USER_DETAILS_ENDPOINT ="/userDetails";

    public static final String REJECTED_LOCATION_REQUEST_ENDPOINT = "/rejectRequest";

    public static final String ACCEPT_HISTORY_ENDPOINT = "/acceptRequestHistory";

    public static final String REJECT_HISTORY_ENDPOINT = "/rejectRequestHistory";

    public  static final String ALL_NOTIFICATIONS="/getAllNotifications";
    //public static final String _ENDPOINT =;
}
