# AP_Camera_Core_Android

## New Features
- Added mulltiple shooting modes.
## How to use version 1.1.0

Create ActivityResultContract for ApCameraContract
```kotlin
val apCameraContract = registerForActivityResult(ApCameraContract("tag_name")) { imageUriStrResultList ->
     //imageUriStrResultList is type ArrayList<String>
     //result from Apcamera
}
```
Create Bundle for launch
```kotlin
val cameraBundle = Bundle().apply {
    putInt(
        ApCameraConst.ApCameraMode.AP_CAMERA_CONST_MODE_NAME,
        ApCameraConst.ApCameraMode.AP_CAMERA_VAL_IS_ONLY_CAMERA_APC_MODE
    )
}
apCameraContract.launch(cameraBundle)
```

## Camera Mode
##### key value name
 ```
 ApCameraConst.ApCameraMode.AP_CAMERA_CONST_MODE_NAME
 ```
##### value for mode
```
ApCameraConst.ApCameraMode.AP_CAMERA_VAL_IS_ONLY_CAMERA_APC_MODE  is default simple mode. Just Capture and Preview
ApCameraConst.ApCameraMode.AP_CAMERA_VAL_VIEW_GALLERY_MODE  is capture and preview with editor and choose photo in gallery
ApCameraConst.ApCameraMode.AP_CAMERA_VAL_MULTIPLE_SHOT_PREVIEW_MODE  is Multiple Shot and Preview with editor
```



&copy; 2022 APThai
 
