package ru.nsu.ccfit.zuev.osu.menu;

import android.app.Activity;
import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
// import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceFragmentCompat;

import com.edlplan.framework.easing.Easing;
import com.edlplan.ui.ActivityOverlay;
import com.edlplan.ui.BaseAnimationListener;
import com.edlplan.ui.fragment.WebViewFragment;
import com.edlplan.ui.EasingHelper;

import java.io.File;
import java.util.Arrays;

import ru.nsu.ccfit.zuev.osu.Config;
import ru.nsu.ccfit.zuev.osu.GlobalManager;
import ru.nsu.ccfit.zuev.osu.LibraryManager;
import ru.nsu.ccfit.zuev.osu.MainActivity;
import ru.nsu.ccfit.zuev.osu.PropertiesLibrary;
import ru.nsu.ccfit.zuev.osu.ResourceManager;
import ru.nsu.ccfit.zuev.osu.ToastLogger;
import ru.nsu.ccfit.zuev.osu.game.SpritePool;
import ru.nsu.ccfit.zuev.osu.helper.StringTable;
import ru.nsu.ccfit.zuev.osu.online.OnlineInitializer;
import ru.nsu.ccfit.zuev.osu.online.OnlineManager;
import ru.nsu.ccfit.zuev.osuplus.R;

public class SettingsMenu extends PreferenceFragmentCompat {

    private Activity mActivity;
    private View root;
    private PreferenceScreen mParentScreen;
    private PreferenceScreen parentScreen;
    private boolean isOnNestedScreen = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = GlobalManager.getInstance().getMainActivity();
        // addPreferencesFromResource(R.xml.options);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = super.onCreateView(inflater, container, savedInstanceState);
        playOnLoadAnim();
        return root;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.options, rootKey);
        reloadSkinList();

        // screens
        mParentScreen = parentScreen = getPreferenceScreen();

        final PreferenceScreen onlineOption = (PreferenceScreen) findPreference("onlineOption");
        onlineOption.setOnPreferenceClickListener(preference -> {
            setPreferenceScreen(onlineOption);
            return true;
        });

        final PreferenceScreen general = (PreferenceScreen) findPreference("general");
        general.setOnPreferenceClickListener(preference -> {
            setPreferenceScreen(general);
            return true;
        });

        final PreferenceScreen color = (PreferenceScreen) findPreference("color");
        color.setOnPreferenceClickListener(preference -> {
            parentScreen = general;
            setPreferenceScreen(color);
            return true;
        });

        final PreferenceScreen sound = (PreferenceScreen) findPreference("sound");
        sound.setOnPreferenceClickListener(preference -> {
            setPreferenceScreen(sound);
            return true;
        });

        final PreferenceScreen beatmaps = (PreferenceScreen) findPreference("beatmaps");
        beatmaps.setOnPreferenceClickListener(preference -> {
            setPreferenceScreen(beatmaps);
            return true;
        });

        final PreferenceScreen advancedOpts = (PreferenceScreen) findPreference("advancedopts");
        advancedOpts.setOnPreferenceClickListener(preference -> {
            setPreferenceScreen(advancedOpts);
            return true;
        });

        // screens END

        final EditTextPreference skinToppref = (EditTextPreference) findPreference("skinTopPath");
        skinToppref.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue.toString().trim().length() == 0) {
                skinToppref.setText(Config.getCorePath() + "Skin/");
                Config.loadConfig(mActivity);
                reloadSkinList();
                return false;
            }

            File file = new File(newValue.toString());
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    ToastLogger.showText(StringTable.get(R.string.message_error_dir_not_found), true);
                    return false;
                }
            }

            skinToppref.setText(newValue.toString());
            Config.loadConfig(mActivity);
            reloadSkinList();
            return false;
        });

        final Preference pref = findPreference("clear");
        pref.setOnPreferenceClickListener(preference -> {
            LibraryManager.getInstance().clearCache(mActivity);
            return true;
        });
        final Preference clearProps = findPreference("clear_properties");
        clearProps.setOnPreferenceClickListener(preference -> {
            PropertiesLibrary.getInstance()
                    .clear(mActivity);
            return true;
        });
        final Preference register = findPreference("registerAcc");
        register.setOnPreferenceClickListener(preference -> {
            OnlineInitializer initializer = new OnlineInitializer(mActivity);
            initializer.createInitDialog();
            return true;
        });

        final Preference update = findPreference("update");
        update.setOnPreferenceClickListener(preference -> {
            new WebViewFragment("https://" + OnlineManager.hostname).show();
            // Beta.checkUpgrade();
            return true;
        });
    }

    public void onNavigateToScreen(PreferenceScreen preferenceScreen) {
        if(preferenceScreen.getKey() != null) {
            isOnNestedScreen = true;
            setTitle(preferenceScreen.getTitle().toString());
            ToastLogger.showText(isOnNestedScreen + ":" + preferenceScreen.getTitle().toString(), false);
        }
    }

    private void setTitle(String title) {
       ((TextView) findViewById(R.id.title)).setText(title); 
    }

    public void onBackPress() {
        if(GlobalManager.getInstance().getSettingsMenu() == null) {
            return;
        }
        if(parentScreen.getKey() != null) {
            setPreferenceScreen(parentScreen);
            setTitle(parentScreen.getTitle().toString());
            parentScreen = mParentScreen;
            ToastLogger.showText("back1", false);
            return;
        }

        if(isOnNestedScreen) {
            isOnNestedScreen = false;
            setPreferenceScreen(mParentScreen);
            setTitle(StringTable.get(R.string.opt_main));
        }else {
           dismiss(); 
        }
        ToastLogger.showText("back", false);
    }

    protected void playOnLoadAnim() {
        View body = findViewById(R.id.body);
        body.setTranslationY(100);
        body.animate().cancel();
        body.animate()
            .translationY(0)
            .setDuration(200)
            .setInterpolator(EasingHelper.asInterpolator(Easing.InOutQuad))
            .start();
    }

    protected void playOnDismissAnim(Runnable runnable) {
        View body = findViewById(R.id.body);
        body.animate().cancel();
        body.animate()
            .translationY(100)
            .setDuration(200)
            .setInterpolator(EasingHelper.asInterpolator(Easing.InOutQuad))
            .setListener(new BaseAnimationListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            })
            .start();
    }

    public View findViewById(@IdRes int id) {
        return root.findViewById(id);
    }

    public SettingsMenu show() {
        if(GlobalManager.getInstance().getSettingsMenu() != null) {
            return null;
        }
        ActivityOverlay.addOverlay(this, this.getClass().getName() + "@" + this.hashCode());
        return this;
    }

    public SettingsMenu dismiss() {
        playOnDismissAnim(() -> {
            ActivityOverlay.dismissOverlay(SettingsMenu.this);
            Config.loadConfig(mActivity);
            GlobalManager.getInstance().getMainScene().reloadOnlinePanel();
            GlobalManager.getInstance().getMainScene().loadTimeingPoints(false);
            GlobalManager.getInstance().setSettingsMenu(null);
            // GlobalManager.getInstance().getSongService().setIsSettingMenu(false);
            GlobalManager.getInstance().getSongService().setGaming(false);
        });
        return this;
    }

    /* 
    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    } */

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void reloadSkinList() {
        try {
            final ListPreference skinPathPref = (ListPreference) findPreference("skinPath");
            File skinMain = new File(Config.getSkinTopPath());
            if (!skinMain.exists()) skinMain.mkdir();
            File[] skinFolders = skinMain.listFiles(file -> file.isDirectory() && !file.getName().startsWith("."));
            CharSequence[] entries = new CharSequence[skinFolders.length + 1];
            CharSequence[] entryValues = new CharSequence[skinFolders.length + 1];
            entries[0] = skinMain.getName() + " (Default)";
            entryValues[0] = skinMain.getPath();
            for (int i = 1; i < entries.length; i++) {
                entries[i] = skinFolders[i - 1].getName();
                entryValues[i] = skinFolders[i - 1].getPath();
            }
            Arrays.sort(entries, 1, entries.length);
            Arrays.sort(entryValues, 1, entryValues.length);
            skinPathPref.setEntries(entries);
            skinPathPref.setEntryValues(entryValues);
            skinPathPref.setOnPreferenceChangeListener((preference, newValue) -> {
                SpritePool.getInstance().purge();
                GlobalManager.getInstance().setSkinNow(newValue.toString());
                ResourceManager.getInstance().loadCustomSkin(newValue.toString());
                GlobalManager.getInstance().getEngine().getTextureManager().reloadTextures();
                ToastLogger.showText(StringTable.get(R.string.message_loaded_skin), true);
                return true;
            });
            skinPathPref.setValueIndex(skinPathPref.findIndexOfValue(Config.getSkinPath()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
