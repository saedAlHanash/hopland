package com.hopeland.pda.example.Helpers.system;//package com.example.qareeb_driver.Helpers.system;
//
//
//import android.Manifest;
//import android.app.Activity;
//import android.content.pm.PackageManager;
//import android.os.Build;
//
//import androidx.core.app.ActivityCompat;
//
//import static com.example.qareeb_driver.AppConfig.Cods.REQUEST_CODE_CALL_PHONE;
//import static com.example.qareeb_driver.AppConfig.Cods.REQUEST_CODE_GPS;
//import static com.example.qareeb_driver.AppConfig.Cods.REQUEST_CODE_READ_EXTERNAL_STORAGE;
//
//public class PermissionsHelper {
//
//    /**
//     * get Call permission
//     * @param activity instance of {@link Activity}
//     * @return boolean value
//     */
//    public static boolean getCallPhonePermission(Activity activity) {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (activity.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
//                activity.requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CODE_CALL_PHONE);
//            } else {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//
//    /**
//     *  get storage permission
//     * @param activity activity instance of {@link Activity}
//     * @return boolean value
//     */
//    public static boolean getREAD_EXTERNAL_STORAGE(Activity activity) {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
//                activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_EXTERNAL_STORAGE);
//            } else
//                return true;
//        }
//        return false;
//    }
//
//
//    /**
//     *  get Location permission
//     * @param activity instance of {@link Activity}
//     * @return boolean value
//     */
//    public static boolean getPermissionLocation(Activity activity) {
//        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
//                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_GPS);
//            }
//        } else {
//            return true;
//        }
//        return false;
//    }
//
///*    public static void getAllPermission(Activity activity) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (activity.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
//                if (activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
//                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
//                            && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
//                        activity.requestPermissions(new String[]{Manifest.permission.CALL_PHONE
//                                        , Manifest.permission.READ_EXTERNAL_STORAGE
//                                        , Manifest.permission.ACCESS_FINE_LOCATION
//                                        , Manifest.permission.ACCESS_COARSE_LOCATION}
//                                , REQUEST_CODE_FOR_ALL_PERMISSIONS);
//                    }
//                }
//            }
//        }
//    }*/
//
//}
