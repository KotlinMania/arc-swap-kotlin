// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "SwiftTestHarness",
    platforms: [
        .macOS("15.0"),
    ],
    dependencies: [
        .package(name: "ArcSwap", path: "../build/SPMPackage/macosArm64/Debug")
    ],
    targets: [
        .testTarget(
            name: "SwiftTestHarnessTests",
            dependencies: [
                .product(name: "ArcSwapLibrary", package: "ArcSwap")
            ],
            linkerSettings: [
                .unsafeFlags([
                    "-L", "../build/swift-test",
                    "-lArcSwap",
                ]),
            ]
        ),
    ],
    swiftLanguageModes: [.v6]
)
