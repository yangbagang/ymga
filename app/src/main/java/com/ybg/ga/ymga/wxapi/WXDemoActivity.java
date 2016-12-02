/**
 * 
 */
package com.ybg.ga.ymga.wxapi;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.util.AppConstat;

/**
 * @author 杨拔纲
 * 
 */
public class WXDemoActivity extends Activity {

	private IWXAPI wxApi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 实例化
		wxApi = WXAPIFactory.createWXAPI(this, AppConstat.WX_APP_ID);
		wxApi.registerApp(AppConstat.WX_APP_ID);
	}

	/**
	 * 分享网页内容
	 */
	public void wechatShare() {
		int flag = 0;//0分享到微信好友,1分享到微信朋友圈
		WXWebpageObject webpage = new WXWebpageObject();
		webpage.webpageUrl = "这里填写链接url";
		WXMediaMessage msg = new WXMediaMessage(webpage);
		msg.title = "这里填写标题";
		msg.description = "这里填写内容";
		// 这里替换一张自己工程里的图片资源
		Bitmap thumb = BitmapFactory.decodeResource(getResources(),
				R.mipmap.logo);
		msg.setThumbImage(thumb);

		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneSession
				: SendMessageToWX.Req.WXSceneTimeline;
		wxApi.sendReq(req);
	}
}
