package com.gaganode.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.gaganode.sdk.MinerSdk;
import com.gaganode.sdk.http_util.HttpResponse;
import com.gaganode.sdk.http_util.Util;
import com.gaganode.sdk.json.simple.JSONObject;
import com.gaganode.sdk.json.simple.JSONValue;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements MinerSdk.LogCallback {

    String upgrade_url="";
    public final int version_check_interval_secs=3600*8*1000; //8 hours

    TextView logText;
    EditText tokenInput;
    Button startMiningButton;
    Button getTokenButton;
    Button checkRewardButton;
    Button upgradeButton;

    private void toastPublic(final String message){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getBaseContext(),""+message, Toast.LENGTH_LONG).show();
            }});
    }

    public void Log(String log){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String currentDateandTime = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                logText.append(currentDateandTime+"   :"+log+"\n");
            }
        });
    }


    private void checkVersion (){
        new Thread() { public void run() {
            while(true){

                try{
                    Thread.sleep(3000); //3 secs
                }catch (Exception e){}

                try{
                    RemoteVersion remoteV = getRemoteVersion();
                    upgrade_url=remoteV.download_url;

                    Log("remote version: "+remoteV.version);
                    Log("current version:"+MinerSdk.getVersion());

                    if (!MinerSdk.getVersion().equals(remoteV.version)){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                upgradeButton.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }catch (Exception e) {
                    Log("checkVersion" + e);
                }finally {
                    try{
                        Thread.sleep(version_check_interval_secs);
                    }catch (Exception e){}
                }
            }
         }}.start();
    }

    class RemoteVersion {
        public String version;
        public String download_url ;
    }

    private RemoteVersion getRemoteVersion() throws Exception{

        RemoteVersion result=new RemoteVersion();

        String package_api_url ="https://api.package.coreservice.io:10443/api/version/32";

        HttpResponse http_resp = Util.HttpGet(package_api_url);
        if (http_resp.response_code!= HttpURLConnection.HTTP_OK){
            throw new Exception("http_resp.response_code error :"+http_resp.response_code);
        }

        JSONObject jsonObj = (JSONObject) JSONValue.parseWithException(http_resp.response_string);
        long meta_status = (long) jsonObj.get("meta_status");
        String meta_msg = (String) jsonObj.get("meta_message");
        if (meta_status!=1){
            throw new Exception("meta_status error: status:"+meta_status+ " msg:"+meta_msg);
        }

        String version = (String) jsonObj.get("version");

        String content = (String) jsonObj.get("content");
        JSONObject contentObj = (JSONObject) JSONValue.parseWithException(content);

        result.version=version;
        result.download_url=(String) contentObj.get("download_url");

        return result;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //
        this.logText=findViewById(R.id.logtext);
        this.tokenInput=findViewById(R.id.TokenInput);
        this.startMiningButton = findViewById(R.id.StartMiningButton);
        this.getTokenButton = findViewById(R.id.GetTokenButton);
        this.checkRewardButton=findViewById(R.id.CheckRewardButton);
        this.upgradeButton=findViewById(R.id.UpgradeButton);
        //
        getTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://dashboard.gaganode.com/install_run"));
                startActivity(browserIntent);
            }
        });
        //
        checkRewardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://dashboard.gaganode.com/reward"));
                startActivity(browserIntent);
            }
        });
        //
        this.upgradeButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(upgrade_url));
                startActivity(browserIntent);
            }
        });
        //
        this.tokenInput.setText( getSharedPreferences("miner_sdk",MODE_PRIVATE).getString("token",""));
        startMiningButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    Log("will restart within 60 secs");
                    String token = tokenInput.getText().toString();
                    ////using sdk
                    SharedPreferences miner_sdk_sp= getSharedPreferences("miner_sdk",MODE_PRIVATE);
                    miner_sdk_sp.edit().putString("token",token).apply();
                    //
                    MinerSdk.setToken(token);
                    MinerSdk.Restart();
                }catch (Exception e){
                    Log("restart mining error:"+e);
                }
            }
        });

        //
        try {

            selfStart_samsung();
            selfStart_xiaomi();
            selfStart_huawei();
            selfStart_oppo();
            selfStart_vivo();
            selfStart_meizu();
            selfStart_letv();
            selfStart_smartisan();

        }catch (Exception e){
            toastPublic(e.toString());
            toastPublic("set whitelist failed ,please set whitelist manully");
        }

        //
        MinerSdk.SetLogCallback(this);
        checkVersion();

        //
        MinerService.StartService(this.getApplicationContext());

    }


    private void showActivity(@NonNull String packageName, @NonNull String activityDir) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityDir));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showActivity(@NonNull String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        startActivity(intent);
    }




    private void selfStart_xiaomi() {
        if (Build.BRAND != null&& Build.BRAND.equalsIgnoreCase("xiaomi")){
            showActivity("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
        }
    }

    private void selfStart_huawei() {
        if(Build.BRAND != null &&  (Build.BRAND.equalsIgnoreCase("huawei") || Build.BRAND.equalsIgnoreCase("honor"))){
            try{
                showActivity("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");
            } catch(Exception e) {
                showActivity("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.bootstart.BootStartActivity");
            }
        }
    }


    private void selfStart_oppo() {
        if (Build.BRAND != null&& Build.BRAND.equalsIgnoreCase("oppo")){
            try{
                showActivity("com.coloros.phonemanager");
            } catch(Exception e1) {
                try{
                    showActivity("com.oppo.safe");
                } catch(Exception e2) {
                    try{
                        showActivity("com.coloros.oppoguardelf");
                    } catch(Exception e3) {
                        showActivity("com.coloros.safecenter");
                    }
                }
            }
        }
    }

    private void selfStart_vivo() {
        if (Build.BRAND != null&& Build.BRAND.equalsIgnoreCase("vivo")){
            showActivity("com.iqoo.secure");
        }
    }

    private void selfStart_meizu() {
        if (Build.BRAND != null&& Build.BRAND.equalsIgnoreCase("meizu")){
            showActivity("com.meizu.safe");
        }
    }


    private void selfStart_samsung() {
        if (Build.BRAND != null&& Build.BRAND.equalsIgnoreCase("samsung")){
            try{
                showActivity("com.samsung.android.sm_cn");
            } catch(Exception e) {
                showActivity("com.samsung.android.sm");
            }
        }
    }


    private void selfStart_letv() {
        if (Build.BRAND != null&& Build.BRAND.equalsIgnoreCase("letv")){
            showActivity("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity");
        }
    }


    private void selfStart_smartisan() {
        if (Build.BRAND != null&& Build.BRAND.equalsIgnoreCase("smartisan")){
            showActivity("com.smartisanos.security");
        }
    }


}