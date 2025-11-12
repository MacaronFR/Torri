//
// Created by Macaron on 23/10/2025.
//

import Foundation
import NearbyConnections

@objc class NearbySwift: NSObject {
    let connectionManager: ConnectionManager
    let advertiser: Advertiser
    let discoverer: Discoverer

    @objc init() {
        connectionManager = ConnectionManager(serviceID: "fr.imacaron.torri", strategy: .pointToPoint)
        connectionManager.delegate = self
    }

    @objc func startAdvertising() {
        advertiser = Advertiser(connectionManager: connectionManager)
        advertiser.delegate = self
        advertiser.startAdvertising(using: "IPhone".data(using: .utf8))
    }

    @objc func stopAdvertising() {
        advertiser.stopAdvertising()
        advertiser = nil
    }

    @objc func startDiscovery() {
        discoverer = Discoverer(connectionManager: connectionManager)
        discoverer.delegate = self
        discoverer.startDiscovery()
    }

    @objc func stopDiscovery() {
        discoverer.stopDiscovery()
        discoverer = nil
    }
}

@objc extension NearbySwift: ConnectionManagerDelegate {
    @objc func connectionManager(
        _ connectionManager: ConnectionManager, didReceive verificationCode: String,
        from endpointID: EndpointID, verificationHandler: @escaping (Bool) -> Void) {
        // Optionally show the user the verification code. Your app should call this handler
        // with a value of `true` if the nearby endpoint should be trusted, or `false`
        // otherwise.
        verificationHandler(true)
    }

    @objc func connectionManager(
        _ connectionManager: ConnectionManager, didReceive data: Data,
        withID payloadID: PayloadID, from endpointID: EndpointID) {
        // A simple byte payload has been received. This will always include the full data.
    }

    @objc func connectionManager(
        _ connectionManager: ConnectionManager, didReceive stream: InputStream,
        withID payloadID: PayloadID, from endpointID: EndpointID,
        cancellationToken token: CancellationToken) {
        // We have received a readable stream.
    }

    @objc func connectionManager(
        _ connectionManager: ConnectionManager,
        didStartReceivingResourceWithID payloadID: PayloadID,
        from endpointID: EndpointID, at localURL: URL,
        withName name: String, cancellationToken token: CancellationToken) {
        // We have started receiving a file. We will receive a separate transfer update
        // event when complete.
    }

    @objc func connectionManager(
        _ connectionManager: ConnectionManager,
        didReceiveTransferUpdate update: TransferUpdate,
        from endpointID: EndpointID, forPayload payloadID: PayloadID) {
        // A success, failure, cancelation or progress update.
    }

    @objc func connectionManager(
        _ connectionManager: ConnectionManager, didChangeTo state: ConnectionState,
        for endpointID: EndpointID) {
        switch state {
        case .connecting: nil
            // A connection to the remote endpoint is currently being established.
        case .connected: nil
            // We're connected! Can now start sending and receiving data.
        case .disconnected: nil
            // We've been disconnected from this endpoint. No more data can be sent or received.
        case .rejected: nil
            // The connection was rejected by one or both sides.
        }
    }
}

@objc extension NearbySwift: AdvertiserDelegate {
    @objc func advertiser(_ advertiser: Advertiser, didReceiveConnectionRequestFrom endpointID: EndpointID, with context: Data, connectionRequestHandler: @escaping (Bool) -> Void) {
        connectionRequestHandler(true)
    }
}

@objc extension NearbySwift: DiscovererDelegate {
    @objc func discoverer(_ discoverer: Discoverer, didFind endpointID: EndpointID, with context: Data) {

    }

    @objc func discoverer(_ discoverer: Discoverer, didLose endpointID: EndpointID) {

    }
}