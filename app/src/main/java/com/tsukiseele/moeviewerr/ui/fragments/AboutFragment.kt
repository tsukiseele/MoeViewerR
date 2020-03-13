package com.tsukiseele.moeviewerr.ui.fragments

import android.content.Context
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen

import com.tsukiseele.moeviewerr.R
import com.tsukiseele.moeviewerr.ui.dialog.RewardDialog

import androidx.fragment.app.FragmentActivity

class AboutFragment : PreferenceFragment() {

    private var onOpenSourcePreference: Preference? = null
    private var onCopyrightPreference: Preference? = null
    private var onDonationPreference: Preference? = null

    private var mContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.preference_about)

        bindView()

        mContext = activity
    }

    private fun bindView() {
        onOpenSourcePreference = this.findPreference(KEY_OPENSOURCE)
        onCopyrightPreference = this.findPreference(KEY_COPYRIGHT)
        onDonationPreference = this.findPreference(KEY_DONATION)
    }

    override fun onPreferenceTreeClick(
        preferenceScreen: PreferenceScreen,
        preference: Preference
    ): Boolean {
        when (preference.key) {
            KEY_DONATION -> RewardDialog().show(
                (activity as FragmentActivity).supportFragmentManager,
                "reward"
            )
        }/*
				new AlertDialog.Builder(mContext)
					.setItems(new String[] {"打开链接", "复制ID"}, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface p1, int pos) {
							switch (pos) {
								case 0 :
									Intent intent = new Intent();
									intent.setAction(Intent.ACTION_VIEW);
									intent.setData(Uri.parse("https://qr.alipay.com/a6x00395eyc0zr3j2j8p125"));
									startActivity(intent);
									break;
								case 1 :
									ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
									// 将文本内容放到系统剪贴板里。
									cm.setText("seele.wong@hotmail.com");
									ToastUtil.makeText(getContext(), "复制成功", ToastUtil.LENGTH_LONG).show();
									break;
							}
						}
					}).show();
				*/
        return super.onPreferenceTreeClick(preferenceScreen, preference)
    }

    companion object {
        val KEY_OPENSOURCE = "onOpenSource"
        val KEY_COPYRIGHT = "onCopyright"
        val KEY_DONATION = "onDonation"
    }
}
