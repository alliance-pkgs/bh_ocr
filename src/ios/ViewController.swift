import UIKit
import AVFoundation
import MobileCoreServices

@available(iOS 10.0, *)
class ViewController: UIViewController, AVCaptureVideoDataOutputSampleBufferDelegate {
    
    let ACTION_CLOSE = "close"
    
    var onPictureSnapped: (String) -> Void
    var onFailure: (String) -> Void
    
    var captureSession = AVCaptureSession()
    var captureDevice: AVCaptureDevice!
    var previewLayer: CALayer!
    
    var takePhoto = false
    
    var instruction = ""
    
    init(instruction: String, _ onPictureSnapped: @escaping (String) -> Void, _ onFailure: @escaping (String) -> Void) {
        self.instruction = instruction
        self.onPictureSnapped = onPictureSnapped
        self.onFailure = onFailure
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @IBOutlet weak var previewView: UIView!
    
    //lock screen orientation to portrait
    var orientations = UIInterfaceOrientationMask.portrait
        override var supportedInterfaceOrientations : UIInterfaceOrientationMask {
        get { return self.orientations }
        set { self.orientations = newValue }
        }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        //lock swipe down gesture
        self.modalPresentationStyle = .fullScreen
        if #available(iOS 13.0, *) {
            self.isModalInPresentation = true
        }
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        prepareCamera()
        self.initUiBg()
        self.initCaptureButton()
        self.initFlashButton()
        self.initExitButton()
        self.initInstructionlabel()
        self.initBg()
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }


    func initCaptureButton() {
        let screenWidth = Int(UIScreen.main.bounds.size.width)
        let x1 = Int(screenWidth) - (Int(Double(screenWidth) * 0.85)) // Align to the center of screen.
        let x = x1 + Int(Double(screenWidth) * 0.55 / 2);
        let captureButton = UIButton()

        let buttonImg = UIImage(named: "tack_pic_btn.png")

        captureButton.setBackgroundImage(buttonImg, for: .normal)

        var bottomSpace = 95;
        if #available(iOS 13.0, *) {
            bottomSpace = 125;
        }
        let y = Int(UIScreen.main.bounds.size.height) - bottomSpace // Align to the bottom of screen

        captureButton.frame = CGRect(origin: CGPoint(x: x, y : y), size: CGSize(width: 50, height: 50))

        captureButton.transform = CGAffineTransform(rotationAngle: CGFloat(Double.pi / 2))

        captureButton.addTarget(self, action: #selector(self.onCapture(_:)), for: .touchUpInside)

        self.view.addSubview(captureButton)
    }
    
    func initFlashButton() {
        let flashButton = UIButton()
        let buttonImg = UIImage(named: "flash_camera_btn.png")
        flashButton.setBackgroundImage(buttonImg, for: .normal)
        
        // Align to the top right on screen, with 20 spacing.
        let x = Int((UIScreen.main.bounds.size.width) - 58)
        let y = 6
        
        flashButton.frame = CGRect(origin: CGPoint(x: x, y : y), size: CGSize(width: 35, height: 35))
        
        flashButton.transform = CGAffineTransform(rotationAngle: CGFloat(Double.pi / 2))
        
        flashButton.addTarget(self, action: #selector(self.onFlashToggle(_:)), for: .touchUpInside)
        
        self.view.addSubview(flashButton)
        
    }
    
    func initExitButton() {
        let exitButton = UIButton()
        let buttonImg = UIImage(named: "camera_back_nomal.png")
        exitButton.setBackgroundImage(buttonImg, for: .normal)
        
        // Align to the top right on screen, with 20 spacing.
        let x = 20, y = 6
        
        exitButton.frame = CGRect(origin: CGPoint(x: x, y : y), size: CGSize(width: 35, height: 35))
        
        exitButton.transform = CGAffineTransform(rotationAngle: CGFloat(Double.pi/2))
        
        exitButton.addTarget(self, action: #selector(self.onExit(_:)), for: .touchUpInside)
        
        self.view.addSubview(exitButton)
        
    }
    
    func initInstructionlabel() {
        let screenWidth = Int(UIScreen.main.bounds.size.width)
        let screenHeight = Int(UIScreen.main.bounds.size.height)
        
        let label = UILabel(frame: CGRect(x: 0, y: 0, width: screenWidth, height: screenHeight));
        
        label.text = self.instruction
        label.font = UIFont.systemFont(ofSize:16)
        label.textAlignment = .center
        
        label.transform = CGAffineTransform(rotationAngle: CGFloat(Double.pi / 2))
        
        self.view.addSubview(label)
    }

    func initBg() {
        let screenWidth = Int(UIScreen.main.bounds.size.width)
        let screenHeight = Int(UIScreen.main.bounds.size.height)
        let x = Int(screenWidth) - (Int(Double(screenWidth) * 0.848)) // Align to the center of screen.
        let y = Int(screenHeight) - (Int(Double(screenHeight)  * 0.90)) // Align to the bottom of screen
        let h = Int(Double(screenHeight) * 0.665)
        let w = Int(Double(screenWidth) * 0.695);

        let tranpView=UIView(frame: CGRect(x:x, y:y, width:w, height:h))
        tranpView.backgroundColor = UIColor.clear
//        tranpView.layer.cornerRadius = 10.0
        tranpView.layer.borderWidth = 3.0
        tranpView.layer.borderColor = UIColor.green.cgColor

        tranpView.alpha = 1
        tranpView.clipsToBounds = true


            self.view.addSubview(tranpView)
    }

    func initUiBg() {

        let bgClr = UIColor.black
        let alphaScore = 0.70
        let screenWidth = Int(UIScreen.main.bounds.size.width)
        let screenHeight = Int(UIScreen.main.bounds.size.height)

        //left view
        let widthLR = Int(Double(screenWidth) * 0.15);
        let left=UIView(frame: CGRect(x:0, y:0, width:widthLR, height:screenHeight) )
        left.backgroundColor = bgClr
        left.alpha = CGFloat(Double(alphaScore))
        left.clipsToBounds = true
        left.layer.masksToBounds = true


        //right view
        let xRight = Int(screenWidth) - (Int(Double(screenWidth) * 0.15))
        let right=UIView(frame: CGRect(x:xRight, y:0, width:widthLR, height:screenHeight) )
        right.backgroundColor = bgClr
        right.alpha = CGFloat(Double(alphaScore))
        right.clipsToBounds = true
        right.layer.masksToBounds = true

        //top view
        let x = Int(screenWidth) - (Int(Double(screenWidth) * 0.85)) // Align to the center of screen.
        let w = Int(screenWidth) - (widthLR+widthLR);
        let hTop = Int(Double(screenHeight) * 0.10)
        let top=UIView(frame: CGRect(x:x, y:0, width:w, height:hTop) )
        top.backgroundColor = bgClr
        top.alpha = CGFloat(Double(alphaScore))
        top.clipsToBounds = true
        top.layer.masksToBounds = true

        //bottom view
        let hBottom = Int(Double(screenHeight) * 0.22)
        let yBottom = Int(screenHeight) - (Int(Double(screenHeight)  * 0.24)) // Align to the bottom of screen
        let bottom=UIView(frame: CGRect(x:x, y:yBottom, width:w, height:hBottom) )
        bottom.backgroundColor = bgClr
        bottom.alpha = CGFloat(Double(alphaScore))
        bottom.clipsToBounds = true
        bottom.layer.masksToBounds = true

        self.view.addSubview(left)
        self.view.addSubview(right)
        self.view.addSubview(top)
        self.view.addSubview(bottom)
    }

    @objc func onCapture(_ sender: UIButton) {
        print("OCR Camera :: Capture button pressed.")
        takePhoto = true
    }

    @objc func onFlashToggle(_ sender: UIButton) {
        print("OCR Camera :: Flash button pressed")

        let avDevice = AVCaptureDevice.default(for: AVMediaType.video)

        // check if the device has torch
        if (avDevice?.hasTorch)! {
            // lock your device for configuration
            do {
                try avDevice?.lockForConfiguration()
            } catch {
                print("OCR Camera :: This device has no available torch.")
            }
            
            // check if your torchMode is on or off. If on turns it off otherwise turns it on
            if (avDevice?.isTorchActive)! {
                avDevice?.torchMode = AVCaptureDevice.TorchMode.off
            } else {
                // sets the torch intensity to 100%
                do {
                    try avDevice?.setTorchModeOn(level: 1.0)
                } catch {
                    print("OCR Camera :: Unable to turn on glash")
                }
            }
            
            // unlock your device
            avDevice?.unlockForConfiguration()
        }
    }
    
    @objc func onExit(_ sender: UIButton) {
        print("OCR Camera :: Exit button pressed.")
        
        DispatchQueue.main.async {
            self.onFailure(self.ACTION_CLOSE)
            self.dismiss(animated: true, completion: nil)
        }
    }
    
    func prepareCamera() {
        self.captureSession.sessionPreset = AVCaptureSession.Preset.hd1280x720
        let availableCameraDevices = AVCaptureDevice.devices(for: AVMediaType.video)
        for device in availableCameraDevices {
            if device.position == .back{
                self.captureDevice = device
                self.beginSession()
            }
        }
    }
    
    func beginSession () {
        let screenWidth = Int(UIScreen.main.bounds.size.width)
        let screenHeight = Int(UIScreen.main.bounds.size.height)
        let x = Int(screenWidth) - (Int(Double(screenWidth) * 0.85)) // Align to the center of screen.
        let y = Int(screenHeight) - (Int(Double(screenHeight)  * 0.90)) // Align to the bottom of screen
        let h = Int(Double(screenHeight) * 0.67)
        let w = Int(Double(screenWidth) * 0.70);
        
        do {
            let captureDeviceInput = try AVCaptureDeviceInput(device: captureDevice)
            captureSession.addInput(captureDeviceInput)
            
        } catch {
            print(error.localizedDescription)
        }
        
        
        let previewLayer = AVCaptureVideoPreviewLayer(session: captureSession)
        previewLayer.frame = CGRect(x:x,y:y,width:w,height:h)
        self.previewLayer = previewLayer
        self.view.layer.addSublayer(self.previewLayer)
        self.view.backgroundColor = UIColor.clear
        self.view.backgroundColor = self.view.backgroundColor!.withAlphaComponent(0)
        self.previewLayer.frame = previewLayer.frame
        captureSession.startRunning()
        
        let dataOutput = AVCaptureVideoDataOutput()
        dataOutput.videoSettings = [(kCVPixelBufferPixelFormatTypeKey as NSString):NSNumber(value:kCVPixelFormatType_32BGRA)] as [String : Any]
        
        dataOutput.alwaysDiscardsLateVideoFrames = true
        
        if captureSession.canAddOutput(dataOutput) {
            captureSession.addOutput(dataOutput)
        }
        
        captureSession.commitConfiguration()
        
        
        let queue = DispatchQueue(label: "com.cv.RemittanceLiteApp")
        dataOutput.setSampleBufferDelegate(self, queue: queue)
            
        
        
    }
    
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        
        if takePhoto {
            print("Do it")
            takePhoto = false
            
            if let image = self.getImageFromSampleBuffer(buffer: sampleBuffer) {
                print("Fire the image!")
                
                let imageData = image.jpegData(compressionQuality: 1.0)!
                let strBase64 = imageData.base64EncodedString()
                
                DispatchQueue.main.async {
                    self.onPictureSnapped(strBase64)
                    self.dismiss(animated: true, completion: nil)
                }
            }
            
            
        }
    }
    
    
    func getImageFromSampleBuffer (buffer:CMSampleBuffer) -> UIImage? {
        if let pixelBuffer = CMSampleBufferGetImageBuffer(buffer) {
            let ciImage = CIImage(cvPixelBuffer: pixelBuffer)
            let context = CIContext()
            
            let imageRect = CGRect(x: 0, y: 0, width: CVPixelBufferGetWidth(pixelBuffer), height: CVPixelBufferGetHeight(pixelBuffer))
            
            if let image = context.createCGImage(ciImage, from: imageRect) {
                return UIImage(cgImage: image, scale: UIScreen.main.scale, orientation: .right)
            }
            
        }
        
        return nil
    }
    
    func stopCaptureSession () {
        self.captureSession.stopRunning()
        
        if let inputs = captureSession.inputs as? [AVCaptureDeviceInput] {
            for input in inputs {
                self.captureSession.removeInput(input)
            }
        }
        
    }
    
    // TODO: Not using yet.
    func initOverlay() {
        let context = UIGraphicsGetCurrentContext()
        context!.setLineWidth(100)
        context!.setStrokeColor(red: 255, green: 255, blue: 0, alpha: 50)
        context?.move(to: CGPoint(x: 500, y: 200))
        context?.addLine(to: CGPoint(x: 500, y: 300))
        context!.strokePath()
    }
    
}
