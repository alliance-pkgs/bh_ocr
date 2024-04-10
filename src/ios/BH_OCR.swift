import Foundation



@available(iOS 10.0, *)
@objc(BH_OCR) class BH_OCR : CDVPlugin {
    
    private var callBackId: String?
    
    @objc(bh_ocr:) func bh_ocr(_ command: CDVInvokedUrlCommand) {
        self.callBackId = command.callbackId
        
        let instruction = command.arguments[0] as? String ?? "";
        
        let viewController = ViewController(
            instruction: instruction,
        
            {(imageBase64: String) -> Void in
                print("OCR Camera :: Execute successful callback")
                self.onCaptureSuccess(imageBase64: imageBase64)
            },
            
            
            {(errorMessage: String) -> Void in
                print("OCR Camera :: Execute successful callback")
                self.onCaptureFailure(errorMessage: errorMessage)
            }
        );
        
        self.viewController.present(viewController, animated: true, completion: nil)
    }
    
    func onCaptureSuccess(imageBase64: String) {
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK,
            messageAs: ["ReturnLPFileName": imageBase64]
        )
        
        self.commandDelegate.send(pluginResult, callbackId: self.callBackId)
    }
    
    func onCaptureFailure(errorMessage: String) {
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_ERROR,
            messageAs: ["description": errorMessage]
        )
        
        self.commandDelegate.send(pluginResult, callbackId: self.callBackId)
    }
    
}
