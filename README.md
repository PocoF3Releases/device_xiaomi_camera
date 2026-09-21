# Xiaomi Camera for POCO F3 (alioth)

![POCO F3 (alioth) — Xiaomi Camera](assets/poco-f3-xiaomi-camera.webp)

Device-side integration and compatibility layer for Xiaomi Camera on the
**POCO F3 / Redmi K40 / Mi 11X (alioth)** family, with the corresponding
`aliothin` device-feature configuration.

This repository is intended to live at:

```text
device/xiaomi/camera
```

The proprietary camera APK and vendor libraries are kept separately in the
companion `vendor/xiaomi/camera` repository.

## What this repository owns

The camera integration is intentionally kept here rather than spread across
the device-common tree. It currently provides:

- Xiaomi Camera permissions, default permissions and sysconfig entries
- CameraX basic extension provider and CamX override settings
- alioth / aliothin device-feature configuration
- Camera SELinux policy
- Camera compatibility shims and vendor-library symlinks
- `MiuiCameraOverlay`
- Camera-related system and vendor properties
- Maintenance patches for alioth camera compatibility

## Integration

### 1. Clone the device-side camera integration

```bash
git clone https://github.com/PocoF3Releases/device_xiaomi_camera.git -b aosp-16 device/xiaomi/camera
```

### 2. Clone the companion proprietary vendor repository

```bash
git clone https://gitlab.com/johnmart19/vendor_xiaomi_camera.git -b aosp-16 vendor/xiaomi/camera
```

### 3. Include Xiaomi Camera from the device tree

Add the following to the device `device.mk`:

```makefile
# Miui Camera
include device/xiaomi/camera/miuicamera.mk
```

That include brings in the camera packages, overlays, properties, SELinux
directories maintained by this repository.

## Compatibility ownership

The mod stores watermarks in app-private storage; it does not require MiSys.

Legacy DisplayConfig compatibility is owned by the sm8250 display HAL. The
display composer publishes a real `vendor.display.config@1.9/default`
compatibility endpoint alongside its native 2.0 service for the proprietary
alioth CamX client, so this camera tree does not carry a DisplayConfig VINTF
matrix or pretend that a compatibility matrix provides the runtime service.

## Repository layout

```text
configs/       Camera configs, permissions, device features and VINTF fragments
patches/      Maintained Xiaomi Camera compatibility patches
rro_overlays/ Xiaomi Camera resource overlays
sepolicy/     Camera SELinux policy
shims/        Compatibility shims
miuicamera.mk Main product integration entry point
```

## Companion repository

Proprietary Xiaomi Camera prebuilts:

https://gitlab.com/johnmart19/vendor_xiaomi_camera

## Android 17 CameraX extension provider

`extensions/` installs `xiaomi-camera-extensions.jar`, registered under the standard
`androidx.camera.extensions.impl` shared-library name. CameraExtensionsProxy and
CameraX clients discover this library without adding it to the boot classpath.
The product also needs the `camera-extensions-basic-contracts` filegroup added to
`frameworks/ex/camera2/extensions/stub/Android.bp`; it shares the platform API
interfaces, not AOSP's demonstration/no-op effect implementations.

The similarly named proprietary `camerax-vendor-extensions.jar` is a separate
Xiaomi SDK (`Camera2VendorEx`), not an AndroidX extension implementation. It remains
packaged for compatibility. On the inspected phone it was boot-mapped but lacked
AndroidX entry points. Its MiCameraDeviceWrapper also accepts an empty MiviInfo
provider response without falling back to the HAL capability table. The standard
provider therefore reads the CamX capability table directly; it does not depend
on MiuiCamera's content provider or make that SDK's methods execute.

The basic API version is 1.1. HDR and Night submit one JPEG capture request with
CamX session operation and algorithm controls; multi-frame processing belongs to
the HAL. Preview uses the same operation with the capture effect disabled. No
software tone-map/pass-through sample is advertised as HDR. Availability requires
CamX capability version 1.0, the enabled flag, a valid six-record table, a matching
camera role/algorithm bit, the session-operation key and JPEG/private outputs.
NORMAL record 0 selects operation 65290 for HDR; SUPER_NIGHT record 2 selects
65292 for Night. The byte-valued effect tags match the installed HAL registry.
This HAL omits those effect tags from availableRequestKeys, so their capability
is gated by the role's algorithm bit rather than that incomplete list.
Auto, Beauty and Bokeh are explicitly unavailable; advanced processing, postview
and progress reporting are not advertised.

Validation on 2026-09-21: device metadata reports HDR for roles 0, 1 and 21;
Night for roles 0 and 1, but not 21. This validates the capability input only.
The new provider has NOT been compiled, installed or capture-tested, per the
no-rebuild instruction. Session creation, processed JPEG correctness, app
compatibility and resource cleanup must be checked after the next user build.
Search logcat for `XiaomiCameraExtensions` to distinguish real session startup
from simply finding the JAR in a process mapping. Test each exposed mode in a
CameraX/Camera2 Extensions client, capture/save/reopen the JPEG, switch cameras,
close/reopen sessions and compare with the unextended capture in the same scene.

When updating blobs, recheck the version/table layout, camera roles, vendor tag
types and operation mapping against the new HAL. Do not loosen capability checks
just to expose modes. When updating the framework extension API, review the shared
contracts before increasing the advertised API version.
