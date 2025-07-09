# proprietary_device_xiaomi_camera

Prebuilt modded MIUI/Leica Camera 5.0 for Mi Pad 6 (Pipa), to include in custom ROM builds.

### How to use?

1. Clone this repo to `device/xiaomi/camera`
2. Clone https://gitlab.com/johnmart19/vendor_xiaomi_camera/-/tree/aosp-15-pipa to 'vendor/xiaomi/camera'
3. Inherit it from `device.mk` in device tree:
```
# Camera
$(call inherit-product-if-exists, device/xiaomi/camera/miuicamera.mk)
```

4. Ensure that you added needed changes for Miui Camera:

#### device_xiaomi_sm8250-common:

 [Set TARGET_CAMERA_PACKAGE_NAME for Miui/Leica Camera](https://github.com/PocoF3Releases/device_xiaomi_sm8250-common/commit/09716da6c781a099007ccb71ffad6dba9e8ea07f)

 [FCM: Import MiSys entries](https://github.com/PocoF3Releases/device_xiaomi_sm8250-common/commit/26b57664835c487db278dbda83fe936ceb831c63)

 [Camera: Enable newer HIDL overrideFormat](https://github.com/PocoF3Releases/device_xiaomi_sm8250-common/commit/0da284303f6c6cf46923becfe292fd1164b10454)

 [Camera: Enable newer HIDL overrideFormat (Another Implementation)](https://github.com/PocoF3Releases/device_xiaomi_sm8250-common/commit/a3bfd6c0ec978e363c022a177b27d25254adcdc8)

 [overlays: Allow HFR for Miui Camera](https://github.com/PocoF3Releases/device_xiaomi_sm8250-common/commit/da3cc9239b02e14480dbba3bce93c8e4e48b0978) 

#### device_xiaomi_pipa:

 [Inherit Miui Camera Repository](https://github.com/Matrixx-Devices/android_device_xiaomi_pipa/commit/1f07bd95b24255ac39b853041d076284fa6c5784)

 [Enable support of MiuiCamera](https://github.com/Matrixx-Devices/android_device_xiaomi_pipa/commit/0bf658977e2fe89935f8c896fb4e9b7d2abcd1ba)

