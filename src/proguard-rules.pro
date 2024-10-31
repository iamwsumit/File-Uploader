# Add any ProGuard configurations specific to this
# extension here.

-keep public class com.sumit.fileuploader.FileUploader {
    public *;
 }
-keeppackagenames gnu.kawa**, gnu.expr**

-optimizationpasses 4
-allowaccessmodification
-mergeinterfacesaggressively

-repackageclasses 'com/sumit/fileuploader/repack'
-flattenpackagehierarchy
-dontpreverify
