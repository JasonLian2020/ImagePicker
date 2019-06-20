package me.jason.imagepicker;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;

import me.jason.imagepicker.internal.entity.CaptureStrategy;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startImagePicker(View view) {
        new RxPermissions(this)
                .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(permission -> {
                    if (permission.granted) {
                        ImagePicker.from(MainActivity.this)
                                .choose(MimeType.ofAll())
                                .capture(true)
                                .captureStrategy(new CaptureStrategy(true, "me.jason.imagepicker.fileprovider", ""))
                                .imageEngine(new Glide4Engine())
                                .forResult(101);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case 101:
                if (data == null) return;
                List<String> selectedPaths = ImagePicker.obtainPathResult(data);
                List<Uri> selectedUris = ImagePicker.obtainUriResult(data);
                for (int i = 0; i < selectedPaths.size(); i++) {
                    Log.d("jason", "path = " + selectedPaths.get(i));
                    Log.d("jason", "uri = " + selectedUris.get(i));
                    Log.d("jason", "==============================");
                }
                break;
        }
    }

    public void permissionStorage(View view) {
        Log.d("jason", "===============permissionStorage===============");
        new RxPermissions(this)
                .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(permission -> {
                    if (permission.granted) {
                        //授权成功
                        Log.d("jason", permission.name + "[授权成功]");
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        //授权失败
                        Log.d("jason", permission.name + "[授权失败]");
                    } else {
                        //授权失败，不能再次询问
                        Log.d("jason", permission.name + "[授权失败，不能再次询问]");
                    }
                });

    }

    public void permissionCamera(View view) {
        Log.d("jason", "===============permissionCamera===============");
        new RxPermissions(this)
                .requestEach(Manifest.permission.CAMERA)
                .subscribe(permission -> {
                    if (permission.granted) {
                        //授权成功
                        Log.d("jason", permission.name + "[授权成功]");
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        //授权失败
                        Log.d("jason", permission.name + "[授权失败]");
                    } else {
                        //授权失败，不能再次询问
                        Log.d("jason", permission.name + "[授权失败，不能再次询问]");
                    }
                });
    }

    public void permissionAudio(View view) {
        Log.d("jason", "===============permissionAudio===============");
        new RxPermissions(this)
                .requestEach(Manifest.permission.RECORD_AUDIO)
                .subscribe(permission -> {
                    if (permission.granted) {
                        //授权成功
                        Log.d("jason", permission.name + "[授权成功]");
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        //授权失败
                        Log.d("jason", permission.name + "[授权失败]");
                    } else {
                        //授权失败，不能再次询问
                        Log.d("jason", permission.name + "[授权失败，不能再次询问]");
                    }
                });
    }

    public void permissionTwo(View view) {
        Log.d("jason", "===============permissionTwo===============");
        new RxPermissions(this)
                .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .subscribe(permission -> {
                    if (permission.granted) {
                        //授权成功
                        Log.d("jason", permission.name + "[授权成功]");
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        //授权失败
                        Log.d("jason", permission.name + "[授权失败]");
                    } else {
                        //授权失败，不能再次询问
                        Log.d("jason", permission.name + "[授权失败，不能再次询问]");
                    }
                });
    }

    public void permissionThree(View view) {
        Log.d("jason", "===============permissionThree===============");
        new RxPermissions(this)
                .requestEach(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                .subscribe(permission -> {
                    if (permission.granted) {
                        //授权成功
                        Log.d("jason", permission.name + "[授权成功]");
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        //授权失败
                        Log.d("jason", permission.name + "[授权失败]");
                    } else {
                        //授权失败，不能再次询问
                        Log.d("jason", permission.name + "[授权失败，不能再次询问]");
                    }
                });
    }
}
