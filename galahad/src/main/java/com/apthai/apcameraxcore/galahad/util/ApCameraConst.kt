package com.apthai.apcameraxcore.galahad.util

object ApCameraConst {

    object ApCameraMode {
        const val AP_CAMERA_CONST_MODE_NAME: String = "ap_camera_mode"

        /**
         * AP_CAMERA_VAL_IS_ONLY_CAMERA_APC_MODE = 0
         * Call camera simple only
         * **/
        const val AP_CAMERA_VAL_IS_ONLY_CAMERA_APC_MODE: Int = 0

        /**
         * AP_CAMERA_CONST_MODE_MULTIPLE_SHOT_PREVIEW = 1
         * Just a capture multiple shot and preview
         * **/
        const val AP_CAMERA_VAL_MULTIPLE_SHOT_PREVIEW_MODE: Int = 1

        /**
         * AP_CAMERA_VAL_VIEW_GALLERY_MODE = 2
         * Select Multiple image in gallery and preview for ap camera folder
         * **/
        const val AP_CAMERA_VAL_VIEW_GALLERY_MODE: Int = 2

        /**
         * AP_CAMERA_VAL_CHOOSE_GALLERY_MULTIPLE_MODE = 2
         * choose gallery multiple image in gallery and preview
         * **/
        const val AP_CAMERA_VAL_CHOOSE_GALLERY_MULTIPLE_MODE: Int = 3
    }

    object ApCameraPayload {

        const val AP_CAMERA_BUNDLE_PAYLOAD_CONST = "ap_camera_screen_bundle_payload_apc"
        const val AP_CAMERA_FACING_TYPE: String = "ap_camera_screen_facing_type_payload_apc"
        const val AP_CAMERA_FLASH_TYPE: String = "ap_camera_screen_flash_type_payload_apc"
        const val AP_CAMERA_ASPECT_RATIO_TYPE: String =
            "ap_camera_screen_aspect_ration_type_payload_apc"
        const val AP_CAMERA_DIRECTORY_ROOT_PATH: String = "ap_camera_screen_directory_root_path_apc"
        const val AP_CAMERA_FILE_NAME: String = "ap_camera_screen_file_name_apc"
        const val AP_CAMERA_IS_ONLY_CALL_CAMERA: String = "ap_camera_screen_is_only_call_camera_apc"
        const val AP_CAMERA_OUTPUT_URI_STRING: String =
            "ap_camera_screen_output_path_uri_string_apc"
        const val AP_CAMERA_INPUT_IMAGE_URI_LIST_CONST_NAME = "ap_camera_input_path_uri_string"
    }

    object ApPreviewPayload {

        const val AP_PREVIEW_URI_STR_PAYLOAD = "ap_preview_screen_uri_path_apc"
    }
}
