package com.tsukiseele.moeviewerr.ui.dialog

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import com.tsukiseele.moeviewerr.R

class RewardDialog : DialogFragment(), View.OnClickListener {
    val WECHAT = 0
    val ALIPAY = 1

    private var mContext: Context? = null
    private var ivQrCode: ImageView? = null

    override fun onClick(view: View) {
        when (view.tag) {
            WECHAT -> ivQrCode!!.setImageResource(R.drawable.qr_wechat_256)
            ALIPAY -> ivQrCode!!.setImageResource(R.drawable.qr_alipay_256)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        viewGroup: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mContext = getContext()

        val container = LinearLayout(mContext)
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        container.layoutParams = params
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(20, 20, 20, 20)

        val tvTitle = TextView(mContext)
        tvTitle.text = "捐助开发者"
        tvTitle.textSize = 20f
        tvTitle.setPadding(20, 20, 20, 20)

        val tvContent = TextView(mContext)
        tvContent.text = "开发软件不易，如果喜欢本软件的话，可以考虑给我打赏哦！"
        tvContent.setPadding(20, 20, 20, 20)

        ivQrCode = ImageView(mContext)
        ivQrCode!!.setImageResource(R.drawable.qr_wechat_256)

        val btnContainer = LinearLayout(mContext)
        btnContainer.layoutParams = params

        val fill = View(mContext)
        val fillPrm = LayoutParams(LayoutParams.WRAP_CONTENT, 0)
        fillPrm.weight = 1f
        fill.layoutParams = fillPrm

        val btnWechat = Button(mContext)
        btnWechat.text = "微信"
        btnWechat.tag = WECHAT
        btnWechat.setPadding(0, 10, 0, 0)

        val btnAlipay = Button(mContext)
        btnAlipay.text = "支付宝"
        btnAlipay.tag = ALIPAY
        btnAlipay.setPadding(0, 10, 0, 0)

        btnContainer.addView(fill)
        btnContainer.addView(btnWechat)
        btnContainer.addView(btnAlipay)

        container.addView(tvTitle)
        container.addView(tvContent)
        container.addView(ivQrCode)
        container.addView(btnContainer)

        btnWechat.setOnClickListener(this)
        btnAlipay.setOnClickListener(this)
        return container
    }

    override fun onStart() {
        super.onStart()
        // 改变窗口尺寸
        val dm = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(dm)

        val window = dialog.window
        val width = (dm.widthPixels * 0.85f).toInt()
        val height = window!!.attributes.height
        window.setLayout(width, height)
    }

    /*
	private static AlertDialog dialog;
	
	public static void show(Context mContext) {
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		LinearLayout container = new LinearLayout(mContext);
		container.setLayoutParams(params);
		container.setOrientation(LinearLayout.VERTICAL);

		final ImageView ivQrCode = new ImageView(mContext);
		ivQrCode.setImageResource(R.drawable.qr_wechat_256);

		LinearLayout btnContainer = new LinearLayout(mContext);
		btnContainer.setLayoutParams(params);

		TextView tvContent = new TextView(mContext);
		tvContent.setText("开发软件不易，如果喜欢本软件的话，可以考虑给我打赏哦");
		container.addView(ivQrCode);
		container.addView(btnContainer);

		//AlertDialog dialog;
		dialog = new AlertDialog.Builder(mContext)
			.setName("打赏")
			.setCancelable(true)
			
			
			.setView(container)
			.setNegativeButton("微信", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface p1, int p2) {
					preventDismissDialog(dialog);
					ivQrCode.setImageResource(R.drawable.qr_wechat_256);
				}
			}).setNeutralButton("支付宝", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface p1, int p2) {
					ivQrCode.setImageResource(R.drawable.qr_alipay_256);
					
				}
			}).setPositiveButton("关闭", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface p1, int p2) {
					p1.cancel();
				}
			})
			.create();
			
			dialog.setCanceledOnTouchOutside(false);
			dialog.show();
	}
	
	private static void preventDismissDialog(AlertDialog dialog) {
		try {
			Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
			field.setAccessible(true);
			//设置mShowing值，欺骗android系统
			field.set(dialog, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
}
