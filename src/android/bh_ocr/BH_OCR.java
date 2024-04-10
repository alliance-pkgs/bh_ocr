package bh_ocr;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import bh_ocr.checkauto.camera.com.DocumentSnapper;
import bh_ocr.checkauto.camera.com.DocumentSnapperRegister;
import bh_ocr.checkauto.camera.com.utill.SDCardUtils;
import bh_ocr.encode_base64.Base64;
import bh_ocr.encode_base64.ByteBase64;

/**
 * This class echoes a string called from JavaScript.
 */
public class BH_OCR extends CordovaPlugin {
	private int nMainID = 13;
	CallbackContext callbackContext;

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		this.callbackContext = callbackContext;
		
		String overlayText = args.getString(0);
		Log.d("TAG", "Languag="+overlayText);

		String myKadReg = args.length() > 1 ? args.getString(1) : null;
		Log.d("TAG", "Languag="+myKadReg);

		if (action.equals("bh_ocr")) {
			if ("MyKadReg".equals(myKadReg)){
				Intent intent = new Intent(cordova.getActivity(), DocumentSnapperRegister.class);
				intent.putExtra("nMainId", nMainID);
				intent.putExtra("flag", 0);
				intent.putExtra("overlayText",overlayText);
				this.cordova.startActivityForResult(this, intent, 8);	
				return true;
			} else {
				Intent intent = new Intent(cordova.getActivity(), DocumentSnapper.class);
				intent.putExtra("nMainId", nMainID);
				intent.putExtra("flag", 0);
				intent.putExtra("overlayText",overlayText);
				this.cordova.startActivityForResult(this, intent, 8);
				return true;
			}
		}

		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		if (requestCode == 8) {
			JSONObject obj = new JSONObject();
			try {
				String path = intent.getStringExtra("HeadJpgPath");
				String imageBase64 = encode(path);
				obj.put("ReturnLPFileName", imageBase64);
				callbackContext.success(obj);
				SDCardUtils.deleteFile(path);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String encode(String path) {
		byte[] imageBinary = ByteBase64.getBytes(path);
		return Base64.encodeToString(imageBinary, Base64.NO_WRAP);
	}
}
