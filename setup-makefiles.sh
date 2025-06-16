#!/bin/bash
#
# Copyright (C) 2016 The CyanogenMod Project
# Copyright (C) 2017-2020 The LineageOS Project
#
# SPDX-License-Identifier: Apache-2.0
#

set -e

DEVICE=camera
VENDOR=xiaomi

# Load extract_utils and do some sanity checks
MY_DIR="${BASH_SOURCE%/*}"
if [[ ! -d "${MY_DIR}" ]]; then MY_DIR="${PWD}"; fi

ANDROID_ROOT="${MY_DIR}/../../.."

export TARGET_ENABLE_CHECKELF=true

HELPER="${ANDROID_ROOT}/tools/extract-utils/extract_utils.sh"
if [ ! -f "${HELPER}" ]; then
    echo "Unable to find helper script at ${HELPER}"
    exit 1
fi
source "${HELPER}"

function vendor_imports() {
    cat <<EOF >>"$1"
                "vendor/xiaomi/alioth",
		"vendor/xiaomi/sm8250-common",
		"hardware/qcom-caf/sm8250",
		"vendor/qcom/opensource/commonsys/display",
		"vendor/qcom/opensource/display",
EOF
}

function lib_to_package_fixup_system_variants() {
    if [ "$2" != "system" ]; then
        return 1
    fi
    case "$1" in
          libmisys_jni.xiaomi | \
          vendor.xiaomi.hardware.misys@1.0 | \
          vendor.xiaomi.hardware.misys@2.0 | \
          vendor.xiaomi.hardware.misys@3.0 | \
          vendor.xiaomi.hardware.misys@4.0)
            echo "${1}_system"
            ;;
        *)
            return 1
            ;;
    esac
}
function lib_to_package_fixup() {
    lib_to_package_fixup_clang_rt_ubsan_standalone "$1" ||
        lib_to_package_fixup_proto_3_9_1 "$1" ||
        lib_to_package_fixup_system_variants "$@"
}

# Initialize the helper
setup_vendor "${DEVICE}" "${VENDOR}" "${ANDROID_ROOT}"

# Warning headers and guards
write_headers

write_makefiles "${MY_DIR}/proprietary-files.txt" true

# Finish
write_footers
