package bh_ocr.encode_base64;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

public class ByteBase64 {

	/**
	 *
	 * 本地图片专byte类型
	 * @param filePath 本地图片路径
	 * @return
	 */
	public static byte[] getBytes(String filePath) {
		byte[] buffer = null;
		try {
			FileInputStream fis = new FileInputStream(filePath);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] b = new byte[1024 * 10];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			fis.close();
			bos.close();
			buffer = bos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return buffer;
	}

}
