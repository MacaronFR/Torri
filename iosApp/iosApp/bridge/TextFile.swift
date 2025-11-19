//
// Created by Kadoc on 19/11/2025.
//

import Foundation
import SwiftUI
import UniformTypeIdentifiers


struct TextFile: FileDocument {
    static var readableContentTypes = [UTType.json]
    
    var text = ""
    
    init(initalText: String = "") {
        text = initalText
    }
    
    init(configuration: ReadConfiguration) throws {
        if let data = configuration.file.regularFileContents {
            text = String(decoding: data, as: UTF8.self)
        }
    }
    
    func fileWrapper(configuration: WriteConfiguration) throws -> FileWrapper {
        let data = Data(text.utf8)
        return FileWrapper(regularFileWithContents: data)
    }
}
