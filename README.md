# Dynamic Blur Effect 

A simple poject demostrating Dynamic Blur Effect with backward compatiblity.

[**Straight to the code**](https://github.com/darkwhite220/DynamicBlurEffect/blob/master/app/src/main/java/earth/darkwhite/dynamicblureffect/MainActivity.kt)

Steps:

  - Redirect parent composable drawing into **Android Picture**. *you need androidx.compose.ui:ui-graphics:1.6.0-alpha01*

  - Create a **Bitmap** from **Picture**

  - **Blur** the bitmap.


[untitled.webm](https://github.com/darkwhite220/DynamicBlurEffect/assets/53045980/6ad01beb-58b6-4bf5-81eb-ea5bc471b711)

## Learning resource

- [A Blurring View for Android](https://developers.500px.com/a-blurring-view-for-android-7f33d41a047d)
- [Redirecting Rendering](https://github.com/android/snippets/blob/main/compose/snippets/src/main/java/com/example/compose/snippets/graphics/AdvancedGraphicsSnippets.kt#L92)
