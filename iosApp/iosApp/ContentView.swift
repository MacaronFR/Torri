import UIKit
import SwiftUI
import UniformTypeIdentifiers
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    private let openFile = OpenFile.shared

    @State private var openExport = false

    @State private var openImport = false

    @State private var document: TextFile = TextFile()

    func initOpenFile() {
        openFile.setOpenFileBrowser = { (value) in
            openImport = value
        }
        openFile.setOpenFileExport = { (value) in
            if(value) {
                document.text = openFile.text
            }
            openExport = value
        }
    }

    var body: some View {
        ComposeView()
            .onAppear(perform: initOpenFile)
            .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
            .fileExporter(isPresented: $openExport, document: document, contentType: UTType.json, onCompletion: { result in
                openFile.onResultSave()
            })
            .fileImporter(isPresented: $openImport, allowedContentTypes: [.text], allowsMultipleSelection: false) { result in
                do {
                    let url = try result.get()
                    openFile.onResult(url)
                } catch {
                    print("\(error)")
                }

            }
    }
}



