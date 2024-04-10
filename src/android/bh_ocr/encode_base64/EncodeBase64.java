package bh_ocr.encode_base64;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;

public class EncodeBase64 extends CordovaPlugin {
	private static final String CallbackContext = null;

	@Override
	public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
		if (action.equals("encodeBase64")) {
			encodeBase64(args, callbackContext);
			return true;
		}

		return false;
	}

	// 二进制压缩成网络传输使用的base64数据
	public void encodeBase64(CordovaArgs args, org.apache.cordova.CallbackContext callbackContext2) {
		String imgPath = null;
		try {
			imgPath = args.getString(0);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			callbackContext2.error(0);
		}
		byte[] binImage1 = ByteBase64.getBytes(imgPath);
		String base64Str = Base64.encodeToString(binImage1, Base64.NO_WRAP);
		callbackContext2.success(base64Str);

	}

}
