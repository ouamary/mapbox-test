package co.nayt.mapboxtest;

import android.content.pm.PackageManager;

/**
 * A simple utility class to provide constants and multiple
 * permission checking.
 */
class Utils {
    // Permission check request code
    static final int LOCATION_REQUEST_CODE = 0;

    // Default location: New York
    static final double DEFAULT_LAT = 40.73581;
    static final double DEFAULT_LNG = -73.99155;

    // Mapbox options
    static final int MAPBOX_ZOOM = 9;
    static final int MAPBOX_DURATION = 3000;

    /**
     * Ensures all runtime permissions are granted.
     *
     * @param grantResults The resulting array from onRequestPermissionsResult
     * @return Boolean, whether permissions are granted or not
     */
    static boolean verifyPermissions(int[] grantResults) {
        if (grantResults.length < 1) {
            return false;
        }

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
