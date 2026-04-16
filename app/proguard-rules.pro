# Keep project-specific rules here when release shrinking is enabled.
-keep class org.bytedeco.javacv.FFmpegFrameGrabber { *; }
-keep class org.bytedeco.javacv.AndroidFrameConverter { *; }
-keep class org.bytedeco.javacpp.Loader { *; }
-keep class org.bytedeco.ffmpeg.** { *; }
-keep class org.bytedeco.javacpp.** { *; }
