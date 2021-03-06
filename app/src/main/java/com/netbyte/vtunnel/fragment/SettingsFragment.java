package com.netbyte.vtunnel.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.netbyte.vtunnel.R;
import com.netbyte.vtunnel.model.AppConst;
import com.netbyte.vtunnel.utils.NetUtil;

public class SettingsFragment extends Fragment {
    Button btnSave;
    EditText editServer, editPath, editDNS, editKey;
    MaterialButtonToggleGroup obfsToggleGroup;
    MaterialButtonToggleGroup protocolToggleGroup;
    SharedPreferences preferences;
    SharedPreferences.Editor preEditor;

    public SettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentActivity activity = this.getActivity();
        assert activity != null;
        View thisView = getView();
        assert thisView != null;
        btnSave = thisView.findViewById(R.id.saveConfigBtn);
        editServer = thisView.findViewById(R.id.editServer);
        editPath = thisView.findViewById(R.id.editPath);
        editKey = thisView.findViewById(R.id.editKey);
        editDNS = thisView.findViewById(R.id.editDNS);
        obfsToggleGroup = thisView.findViewById(R.id.obfsToggleGroup);
        protocolToggleGroup = thisView.findViewById(R.id.protocolToggleGroup);

        preferences = activity.getSharedPreferences(AppConst.APP_NAME, Activity.MODE_PRIVATE);
        preEditor = preferences.edit();

        editServer.setText(preferences.getString("server", AppConst.DEFAULT_SERVER_ADDRESS));
        editPath.setText(preferences.getString("path", AppConst.DEFAULT_PATH));
        editDNS.setText(preferences.getString("dns", AppConst.DEFAULT_DNS));
        editKey.setText(preferences.getString("key", AppConst.DEFAULT_KEY));
        obfsToggleGroup.check(preferences.getBoolean("obfs", false) ? R.id.btnObfsOn : R.id.btnObfsOff);
        protocolToggleGroup.check(preferences.getBoolean("wss", true) ? R.id.btnProtocolWss : R.id.btnProtocolWs);
        btnSave.setOnClickListener(v -> {
            String server = editServer.getText().toString().trim();
            String path = editPath.getText().toString().trim();
            String key = editKey.getText().toString().trim();
            boolean obfs = obfsToggleGroup.getCheckedButtonId() == R.id.btnObfsOn;
            boolean wss = protocolToggleGroup.getCheckedButtonId() == R.id.btnProtocolWss;
            if (!NetUtil.checkServer(server, path, key, wss)) {
                Toast.makeText(activity, R.string.msg_error_server, Toast.LENGTH_LONG).show();
                return;
            }
            String dns = editDNS.getText().toString().trim();
            if (!NetUtil.checkDNS(dns)) {
                Toast.makeText(activity, R.string.msg_error_dns, Toast.LENGTH_LONG).show();
                return;
            }
            preEditor.putString("server", server);
            preEditor.putString("path", path);
            preEditor.putString("dns", dns);
            preEditor.putString("key", key);
            preEditor.putBoolean("obfs", obfs);
            preEditor.putBoolean("wss", wss);
            preEditor.apply();
            Toast.makeText(activity, R.string.msg_success_save, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
