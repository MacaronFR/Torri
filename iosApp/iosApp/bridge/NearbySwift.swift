import Foundation
import NearbyConnections

@objc(NearbySwift) class NearbySwift: NSObject {
    let connectionManager: ConnectionManager
    var advertiser: Advertiser?
    var discoverer: Discoverer?
    var name = "Iphone"

    @objc public var onDiscoverDevice: (String, String) -> Void = { (p1, p2) -> Void in }
    @objc public var onDeviceLost: (String) -> Void = { (p1) -> Void in }
    @objc public var onConnectionStateChange: (String, String) -> Void = { (p1, p2) -> Void in }

    @objc override init() {
        connectionManager = ConnectionManager(serviceID: "fr.imacaron.torri", strategy: .pointToPoint)
        super.init()
        connectionManager.delegate = self
    }

    @objc public func startAdvertising() {
        advertiser = Advertiser(connectionManager: connectionManager)
        advertiser?.delegate = self
        advertiser?.startAdvertising(using: "IPhone".data(using: .utf8)!)
    }

    @objc public func stopAdvertising() {
        advertiser?.stopAdvertising()
        advertiser = nil
    }

    @objc public func startDiscovery() {
        discoverer = Discoverer(connectionManager: connectionManager)
        discoverer?.delegate = self
        discoverer?.startDiscovery()
    }

    @objc public func stopDiscovery() {
        discoverer?.stopDiscovery()
        discoverer = nil
    }

    @objc public func connect(endpointId: EndpointID) {
        discoverer?.requestConnection(to: endpointId, using: name.data(using: .utf8)!)
    }

    @objc public func disconnect(endpointId: EndpointID) {
        do {
            try connectionManager.disconnect(from: endpointId)
        } catch {
            print("\(error)")
        }
    }

    @objc public func sendData(data: Data, endpointId: EndpointID) {
        connectionManager.send(data, to: [endpointId])
    }

    private var data: Data? = nil
    private var status: TransferUpdate = .failure

    @objc public func receiveData() async throws -> Data? {
        var success = false
        while(!success) {
            try await Task.sleep(for: .milliseconds(200))
            switch status {
            case .success:
                success = true
                break
            default: continue
            }
        }
        return data
    }
}

extension NearbySwift: ConnectionManagerDelegate {
    func connectionManager(
        _ connectionManager: ConnectionManager, didReceive verificationCode: String,
        from endpointID: EndpointID, verificationHandler: @escaping (Bool) -> Void) {
        verificationHandler(true)
    }

    func connectionManager(
        _ connectionManager: ConnectionManager, didReceive data: Data,
        withID payloadID: PayloadID, from endpointID: EndpointID) {
        self.data = data
    }

    func connectionManager(
        _ connectionManager: ConnectionManager, didReceive stream: InputStream,
        withID payloadID: PayloadID, from endpointID: EndpointID,
        cancellationToken token: CancellationToken) {
        // We have received a readable stream.
    }

    func connectionManager(
        _ connectionManager: ConnectionManager,
        didStartReceivingResourceWithID payloadID: PayloadID,
        from endpointID: EndpointID, at localURL: URL,
        withName name: String, cancellationToken token: CancellationToken) {
        // We have started receiving a file. We will receive a separate transfer update
        // event when complete.
    }

    func connectionManager(
        _ connectionManager: ConnectionManager,
        didReceiveTransferUpdate update: TransferUpdate,
        from endpointID: EndpointID, forPayload payloadID: PayloadID) {
        status = update
    }

    func connectionManager(
        _ connectionManager: ConnectionManager, didChangeTo state: ConnectionState,
        for endpointID: EndpointID) {
        print(endpointID, state)
        switch state {
        case .connecting: onConnectionStateChange("CONNECTING", endpointID)
        case .connected: onConnectionStateChange("CONNECTED", endpointID)
        case .disconnected: onConnectionStateChange("DISCONNECTED", endpointID)
        case .rejected: onConnectionStateChange("REJECTED", endpointID)
        }
    }
}

extension NearbySwift: AdvertiserDelegate {
    func advertiser(_ advertiser: Advertiser, didReceiveConnectionRequestFrom endpointID: EndpointID, with context: Data, connectionRequestHandler: @escaping (Bool) -> Void) {
        connectionRequestHandler(true)
    }
}

extension NearbySwift: DiscovererDelegate {
    func discoverer(_ discoverer: Discoverer, didFind endpointID: EndpointID, with context: Data) {
        guard let endpointName = String(data: context, encoding: .utf8) else {
            return
        }
        onDiscoverDevice(endpointID, endpointName)
    }

    func discoverer(_ discoverer: Discoverer, didLose endpointID: EndpointID) {
        onDeviceLost(endpointID)
    }
}
