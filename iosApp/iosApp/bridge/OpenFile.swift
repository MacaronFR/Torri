import Foundation

@objc(OpenFile) class OpenFile: NSObject {

    @objc public static let shared = OpenFile()
    
    @objc public var text: String = ""

    @objc public var onResult: ([URL]) -> Void = { (p1) -> Void in }

    @objc public var onResultSave: () -> Void = { () -> Void in }

    @objc public var setOpenFileBrowser: (Bool) -> Void = { (p1) -> Void in }

    @objc public var setOpenFileExport: (Bool) -> Void = { (p1) -> Void in }
}
