package co.work.fukouka.happ.fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.activity.ChangeEmailActivity;
import co.work.fukouka.happ.activity.ChangePasswordActivity;
import co.work.fukouka.happ.activity.EditProfileActivity;
import co.work.fukouka.happ.activity.LanguageSettingsActivity;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.utils.SessionManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfigurationFragment extends Fragment{

    @BindView(R.id.tv_edit_profile) TextView tvEditProfile;
    @BindView(R.id.tv_change_email) TextView tvChangeEmail;
    @BindView(R.id.tv_change_pass) TextView tvChangePass;
    @BindView(R.id.tv_lang_setting) TextView tvLangSetting;
    @BindView(R.id.tv_logout) TextView tvLogout;
    @BindView(R.id.tv_to_logout) TextView tvToLogout;
    @BindView(R.id.tv_app_version) TextView tvAppVersion;
    @BindView(R.id.tv_basic_info) TextView tvBasicInfo;
    @BindView(R.id.tb_title) TextView tbTitle;

    private SessionManager mSession;
    private GetSystemValue systemValue;
    private HappHelper mHelper;

    public ConfigurationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuration, container, false);
        ButterKnife.bind(this, view);

        mSession = new SessionManager(getActivity());
        systemValue = new GetSystemValue(getActivity());
        mHelper = new HappHelper(getActivity());

        assignSystemValues();

        return view;
    }

    @OnClick({R.id.cl_edit_profile, R.id.cl_change_email, R.id.cl_change_password,
            R.id.cl_lang_settings, R.id.cl_logout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cl_edit_profile:
                startActivity(new Intent(getActivity(), EditProfileActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case R.id.cl_change_email:
                startActivity(new Intent(getActivity(), ChangeEmailActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case R.id.cl_change_password:
                startActivity(new Intent(getActivity(), ChangePasswordActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case R.id.cl_lang_settings:
                startActivity(new Intent(getActivity(), LanguageSettingsActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case R.id.cl_logout:
                showAlertDialog();
                break;
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mHelper.logOut());
        builder.setMessage(mHelper.logoutPrompt());

        builder.setPositiveButton(mHelper.logOut(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSession.logoutUser();
                    }
                });

        builder.setNegativeButton(mHelper.cancel(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

    private String getAppVersion() {
        String version = null;
        try {
            PackageInfo pInfo = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0);
            version = pInfo.versionName;
            int verCode = pInfo.versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }

    private void assignSystemValues() {
        HappHelper helper = new HappHelper(getActivity());
        GetSystemValue systemValue = new GetSystemValue(getActivity());

        String changeProfile = systemValue.getValue("title_edit_profile");
        String changeEmail = systemValue.getValue("label_e-mail_address_change");
        String changePassword = systemValue.getValue("label_change-password");
        String langSettings = systemValue.getValue("title_language_settings");
        String logOut = systemValue.getValue("label_logout");
        String toLogOut = systemValue.getValue("btn_logout");
        String version = systemValue.getValue("version_label");
        String basicInfo = systemValue.getValue("subtitle_basic_information");
        String configuration = systemValue.getValue("menu_configuration");

        String appVersion = getAppVersion();

        helper.setText(tvEditProfile, changeProfile);
        helper.setText(tvChangeEmail, changeEmail);
        helper.setText(tvChangePass, changePassword);
        helper.setText(tvLangSetting, langSettings);
        helper.setText(tvLogout, logOut);
        helper.setText(tvToLogout, toLogOut);
        helper.setText(tvBasicInfo, basicInfo);
        helper.setText(tbTitle, configuration);

        if (appVersion != null) {
            helper.setText(tvAppVersion, version + " " +appVersion);
        }
    }

}
