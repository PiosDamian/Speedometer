package piosdamian.pl.speedometer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import piosdamian.pl.speedometer.service.FloatingWidgetService;
import piosdamian.pl.speedometer.service.GPSService;

public class MainActivity extends AppCompatActivity {
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;
    private static final int PERMISSIONS_MULTIPLE_REQUEST = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        } else {
            initializeView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeView();
    }

    private void initializeView() {
        final LocationManager manager = (LocationManager) getSystemService(this.getApplicationContext().LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showSettingsAlert();
        } else {
            if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                startService();
            else
                requestPermissions();
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_MULTIPLE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            if (resultCode == RESULT_OK) {
                initializeView();
            } else {
                Toast.makeText(this, R.string.permission_danied, Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_MULTIPLE_REQUEST) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startService();
            } else {
                Toast.makeText(MainActivity.this, R.string.permission_danied, Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void startService() {
        startService(new Intent(MainActivity.this.getApplicationContext(), FloatingWidgetService.class));
        finish();
    }

    private void showSettingsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.gps_dialog_title);

        builder.setMessage(R.string.gps_not_enable);

        builder.setPositiveButton(R.string.settings, (dialog, which) -> {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.cancel();
            finish();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this.getApplicationContext(), R.color.colorPrimaryDark));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this.getApplicationContext(), R.color.colorPrimaryDark));
    }
}
