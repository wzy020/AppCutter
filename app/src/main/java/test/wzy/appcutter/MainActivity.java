package test.wzy.appcutter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        exeShell("chmod 777 "+getPackageCodePath());
        // if false

        PackageManager pm = getPackageManager();
        ComponentName n = new ComponentName("com.sb.app","com.sb.app.SettingActivity");

//        try {
//            exeShell("pm disable "+n.getPackageName()+"/"+n.getClassName());
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            int r = pm.getComponentEnabledSetting(n);
//            ((TextView)findViewById(R.id.text)).setText(r+"");
//        }

        try {
            PackageInfo info = null;
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.GET_ACTIVITIES);//所有Activity 可以查看disabled状态
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.GET_CONFIGURATIONS);
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.GET_GIDS);
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.GET_INSTRUMENTATION);
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.GET_INTENT_FILTERS);
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.GET_META_DATA);
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.GET_PERMISSIONS);
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.GET_PROVIDERS);
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.GET_RECEIVERS);
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.GET_SERVICES);
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.GET_SHARED_LIBRARY_FILES);
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.GET_SIGNATURES);
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.GET_URI_PERMISSION_PATTERNS);
//
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.MATCH_UNINSTALLED_PACKAGES);
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.MATCH_DISABLED_COMPONENTS);
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS);
//            //info = pm.getPackageInfo("com.bitcan.app", PackageManager.MATCH_SYSTEM_ONLY);
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.GET_DISABLED_COMPONENTS);
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS);
//            info = pm.getPackageInfo("com.bitcan.app", PackageManager.GET_UNINSTALLED_PACKAGES);

            File apk = new File("/data/app/com.bitcan.app-1/base.apk");
            parseApk(apk);
            apk.canExecute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean exeShell(String cmd) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }

    private void parseApk(File apkFile) {
        try {
            Class<?> clazz = Class.forName("android.content.pm.PackageParser");
            //Object packageParser = MethodUtils.invokeConstructor(clazz, apkFile.getAbsolutePath());
            //Constructor c=clazz.getConstructor(String.class);
            //c.setAccessible(true);
            Object packageParser = clazz.newInstance();

            //Object packageObj = MethodUtils.invokeMethod(packageParser, "parsePackage", apkFile, apkFile.getAbsolutePath(), 0);
            Method parsePackage=clazz.getDeclaredMethod("parsePackage", File.class, int.class);
            parsePackage.setAccessible(true);
            Object packageObj = parsePackage.invoke(packageParser, apkFile, 0);

            //List activies = (List) FieldUtils.readField(packageObj, "activities");
            Field activities = packageObj.getClass().getDeclaredField("activities");
            activities.setAccessible(true);
            List activies = (List) activities.get(packageObj);

            for (Object data : activies) {
                //List<IntentFilter> filters = (List) FieldUtils.readField(data, "intents");
                Field intents = data.getClass().getDeclaredField("intents");
                intents.setAccessible(true);
                List<IntentFilter> filters = (List) intents.get(data);

                for (IntentFilter filter : filters) {
                    for (int i = 0; i < filter.countActions(); i++) {
                        Log.w("bush", "filter: " + filter.getAction(i));
                    }

                    for (int i = 0; i < filter.countCategories(); i++) {
                        Log.w("bush", "filter: " + filter.getCategory(i));
                    }
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


}
