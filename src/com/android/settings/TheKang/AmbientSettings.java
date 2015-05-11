/*
 *  Copyright (C) 2015 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.android.settings.TheKang;

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.SwitchPreference;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Button;
import android.util.MathUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.cyanogenmod.SystemSettingSwitchPreference;
import com.android.settings.TheKang.SeekBarPreference;

import java.util.ArrayList;
import java.util.List;

public class AmbientSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnPreferenceClickListener,
        Indexable {
    private static final String TAG = "AmbientSettings";

    private static final String KEY_DOZE = "doze";
    private static final String KEY_DOZE_BRIGHTNESS = "doze_brightness";

    private SwitchPreference mDozePreference;
    private SeekBarPreference mDozeBrightness;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Activity activity = getActivity();
        ContentResolver resolver = getActivity().getContentResolver();
        final Resources resources = getActivity().getResources();

        addPreferencesFromResource(R.xml.ambient_settings);

        mDozePreference = (SwitchPreference) findPreference(KEY_DOZE);
        mDozePreference.setOnPreferenceChangeListener(this);

        updateDozeOptions();

        mDozeBrightness = (SeekBarPreference) findPreference(KEY_DOZE_BRIGHTNESS);
        int dozeBrightness = Settings.System.getInt(resolver,
                    Settings.System.DOZE_BRIGHTNESS, clampAbsoluteBrightness(resources.getInteger(
                com.android.internal.R.integer.config_screenBrightnessSettingMinimum)));
        mDozeBrightness.setValue(dozeBrightness);
        mDozeBrightness.setOnPreferenceChangeListener(this);
    }

    private static int clampAbsoluteBrightness(int value) {
        return MathUtils.constrain(value, PowerManager.BRIGHTNESS_OFF, PowerManager.BRIGHTNESS_ON);
    }

    private static boolean isDozeAvailable(Context context) {
        String name = Build.IS_DEBUGGABLE ? SystemProperties.get("debug.doze.component") : null;
        if (TextUtils.isEmpty(name)) {
            name = context.getResources().getString(
                    com.android.internal.R.string.config_dozeComponent);
        }
        return !TextUtils.isEmpty(name);
    }

    private void updateDozeOptions() {
        //
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
        updateDozeOptions();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void updateState() {
        // Update doze if it is available.
        if (mDozePreference != null) {
            int value = Settings.Secure.getInt(getContentResolver(), Settings.Secure.DOZE_ENABLED, 1);
            mDozePreference.setChecked(value != 0);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        //updateDozeOptions();
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (preference == mDozePreference) {
            boolean value = (Boolean) objValue;
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.DOZE_ENABLED, value ? 1 : 0);
        }
        if (preference == mDozeBrightness) {
            int dozeBrightness = (Integer) objValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.DOZE_BRIGHTNESS, dozeBrightness);
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.ambient_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    if (!isDozeAvailable(context)) {
                        result.add(KEY_DOZE);
                    }
                    return result;
                }
            };
}
