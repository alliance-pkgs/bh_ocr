var exec = require('cordova/exec');

	// 启动护照识别SDK
	exports.bh_ocr = function(success, error, args) {
		exec(success, error, "BH_OCR", "bh_ocr", args);
	};

	// 启动work permit识别SDK
	exports.workpermit = function(success, error, args) {
		exec(success, error, "BH_OCR", "workpermit", args);
	};

	// 调用压图片接口
	exports.encode = function(arg0, success, error) {
		exec(success, error, "BH_OCR", "encode", arg0);
	}