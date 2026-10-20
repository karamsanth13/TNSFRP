package com.tnsfrp.util;

import java.util.List;

/** The single source of truth for the 38 Tamil Nadu districts (the frontend reads it via GET /api/districts). */
public final class Districts {
    public static final List<String> ALL = List.of(
            "Ariyalur", "Chengalpattu", "Chennai", "Coimbatore", "Cuddalore", "Dharmapuri", "Dindigul",
            "Erode", "Kallakurichi", "Kancheepuram", "Kanniyakumari", "Karur", "Krishnagiri", "Madurai",
            "Mayiladuthurai", "Nagapattinam", "Namakkal", "Nilgiris", "Perambalur", "Pudukkottai",
            "Ramanathapuram", "Ranipet", "Salem", "Sivagangai", "Tenkasi", "Thanjavur", "Theni",
            "Thoothukudi", "Tiruchirappalli", "Tirunelveli", "Tirupathur", "Tiruppur", "Tiruvallur",
            "Tiruvannamalai", "Tiruvarur", "Vellore", "Viluppuram", "Virudhunagar");

    private Districts() {}

    public static boolean isValid(String district) {
        return district != null && ALL.contains(district);
    }
}
