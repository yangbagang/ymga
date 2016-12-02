package com.ybg.ga.ymga.wxapi;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.util.AppConstat;

import android.app.Activity;
import android.os.Bundle;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

	// IWXAPI 是第三方app和微信通信的openapi接口
	private IWXAPI api;
	private YbgApp ybgApp = YbgApp.getInstance();

	@Override
	public void onReq(BaseReq arg0) {
		System.out.println("set request to weixin.");
	}

	@Override
	public void onResp(BaseResp resp) {
		switch (resp.errCode) {
		case BaseResp.ErrCode.ERR_OK:
			// 分享成功
			ybgApp.showMessage(getApplicationContext(), "分享成功");
			break;
		case BaseResp.ErrCode.ERR_USER_CANCEL:
			// 分享取消
			ybgApp.showMessage(getApplicationContext(), "分享取消");
			break;
		case BaseResp.ErrCode.ERR_AUTH_DENIED:
			// 分享拒绝
			ybgApp.showMessage(getApplicationContext(), "分享拒绝");
			break;
		}
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		api = WXAPIFactory.createWXAPI(this, AppConstat.WX_APP_ID, false);
		api.handleIntent(getIntent(), this);
	}

}
