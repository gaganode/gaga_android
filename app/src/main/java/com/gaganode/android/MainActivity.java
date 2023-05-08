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


public class MainActivity extends AppCompatActivity implements MinerSdk.LogCallback {

    String upgrade_download_url="";

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

    public void Log(String log) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logText.append(log + "\n");
            }
        });
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
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://dashboard.gaganode.com/mining_reward"));
                startActivity(browserIntent);
            }
        });
        //
        this.upgradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(upgrade_download_url));
                startActivity(browserIntent);
            }
        });


        //
        this.tokenInput.setText( getSharedPreferences("miner_sdk",MODE_PRIVATE).getString("token",""));
        startMiningButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    /////////
                    MinerService.DisableBatteryKill(MainActivity.this);

                    String token = tokenInput.getText().toString();
                    ////using sdk
                    SharedPreferences miner_sdk_sp= getSharedPreferences("miner_sdk",MODE_PRIVATE);
                    miner_sdk_sp.edit().putString("token",token).apply();
                    //
                    MinerSdk.setToken(token);
                    MinerSdk.restart();
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
            toastPublic("set whitelist failed ,please set whitelist manually");
        }

        //
        MinerSdk.SetLogCallback(this);
        //
        MinerSdk.setProduct(Build.BRAND+":"+Build.MODEL);
        MinerService.StartService(this.getApplicationContext());
        //
        checkUpgrade();
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



    public void checkUpgrade() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                MinerSdk.upgradeInfo up_info = MinerSdk.checkUpgrade();
                if (up_info.need_upgrade) {
                    //
                    Log("remote version:" + up_info.remote_version.version);
                    Log("local version:" + MinerSdk.getPackageVersion());
                    Log("please upgrade");
                    //

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.upgrade_download_url = up_info.remote_version.download_url;
                            upgradeButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }).start();
    }



}