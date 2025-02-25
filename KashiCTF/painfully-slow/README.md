# Painfully slow

An easy (or medium?) android reversing challenge

- 499 points
- 11 solves

## Challenge Description

The new security update made this app so painfully slow I had to sell my degree for a Pixel 9 to get it running. But there's another problem now: I can't restore my old backup...

## Challenge Attachments

- [attachments/v1.0.0.apk](attachments/v1.0.0.apk)
- [secure-notes-backup.bkp](attachments/secure-notes-backup.bkp)

## Writeups

##### ~~Synopsis~~ Schizo Rambling

The provided backup file doesn't make any sense. Trying to import the backup in the app results in a failure, even though backup-restore functionality of one's own notes seems to be working in the application. All the backups made by the app, however, seem to have one something in common: their sizes are all multiples of 16 bytes, hinting at encryption.

Hence, we search for stuff like `cipher.doFinal` (common in the java ecosystem) in the jadx decompiled apk, which quickly leads to only a couple of non-trivial instances of its use. They're apparently related to encryption/decryption of the provided backup file. The SecretKeySpec instance used takes a byte array as the key, and the value of the key (as well as the encryption scheme) can be inferred in one of many possible ways: directly reversing a bunch of preceding lines, or better yet, by inserting logging calls in the smali'd bytecode or by simply using frida hooks. The key is derived using last 6 bytes of a device-specific id (that's why the notes backup cannot be decrypted in the app), added and multiplied with a constant to make it 16-bytes long. Those 6 bytes can easily be bruteforced to give the key. Note that the decrypted output will need to be sorted by color (after all, it's an app feature) to give the flag.

##### Detailed writeup

There already exist many good writeups (e.g. [here](https://github.com/Cryptonite-MIT/Write-ups/tree/master/KashiCTF-2025/rev/Painfully%20Slow) and [here](https://medium.com/@fedoraman/kashi-ctf-2025-reverse-engineering-writeup-painfully-slow-f44819a16894?source=friends_link&sk=246e40b057cc804a4918e4b0bba0fbc6)).


## Making the challenge

Creating an app from scratch just for the challenge was not my focus, so I picked the fodder from [here](https://github.com/philipplackner/CleanArchitectureNoteApp). That's a starter note taking app with a very basic set of features.

I'll be honest: resolving the obsolete dependencies and getting that old app to build took so much time I could've made something with the same functionality from scratch in that time. Now that'd have an added benefit of being more difficult to reverse engineer as it'd obviously not follow clean architecture.

Anyways, after I got it to build, I added a triple-dot menu with backup and restore options, serializing/deserializing the notes into json. The following being the relevant code:

```kotlin
// NotesViewModel.kt
            is NotesEvent.BackupNotes -> {
                viewModelScope.launch {
                    val notes = noteUseCases.exportNotes()
                    val id = Settings.Secure.getString(
                        event.ctx.contentResolver, Settings.Secure.ANDROID_ID
                    ).takeLast(6).toInt(radix = 16)
                    val shift = 6969696969696969696L
                    val scale = 6969696969696969696L
                    val extendedKey = id.toBigInteger().add(shift.toBigInteger()).multiply(scale.toBigInteger()).toByteArray()


                    val backupData = Encryption.aesEncrypt(notes.toByteArray(), extendedKey.sliceArray(extendedKey.size-16 until extendedKey.size))

                    val values = ContentValues()
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, "secure-notes-backup.bkp")
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/")

                    // Insert the new file into the MediaStore
                    val resolver = event.ctx.contentResolver
                    val uri = resolver . insert (MediaStore.Files.getContentUri("external"), values)

                    uri?.let {
                        resolver.openOutputStream(uri)?.use {
                            it.write(backupData)
                            Toast.makeText(event.ctx,"Saved to Internal-Storage/Download/secure-notes-backup.bkp",Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            is NotesEvent.RestoreNotes -> {
                val id = Settings.Secure.getString(
                    event.ctx.contentResolver, Settings.Secure.ANDROID_ID
                ).takeLast(6).toInt(radix = 16)
                val shift = 6969696969696969696L
                val scale = 6969696969696969696L
                val extendedKey = id.toBigInteger().add(shift.toBigInteger()).multiply(scale.toBigInteger()).toByteArray()

                try {
                    val restoredData = Encryption.aesDecrypt(event.fileBytes, extendedKey.sliceArray(extendedKey.size-16 until extendedKey.size))
                    val listType = object : TypeToken<ArrayList<Note>>(){}.type
                    val notes: List<Note> = Gson().fromJson(restoredData.decodeToString(), listType)
                    viewModelScope.launch {
                        noteUseCases.replaceAll(notes)
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(event.ctx,"Restored Notes Successfully",Toast.LENGTH_LONG).show()
                        }
                    }
                }catch (e: Exception){
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(event.ctx,"Error: Invalid or corrupt backup",Toast.LENGTH_LONG).show()
                    }
                }
            }

```

```kotlin
object Encryption {
    fun aesEncrypt(data: ByteArray, secretKey: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secureRandom = SecureRandom()
        val iv = ByteArray(16)
        secureRandom.nextBytes(iv)
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(secretKey, 0, 16, "AES"), ivParameterSpec)
        return iv + cipher.doFinal(data)
    }
    fun aesDecrypt(encryptedData: ByteArray, secretKey: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivParameterSpec = IvParameterSpec(encryptedData.sliceArray(0 until 16))
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(secretKey, 0, 16, "AES"), ivParameterSpec)
        return cipher.doFinal(encryptedData.sliceArray(16 until encryptedData.size))
    }
}
```

So I just took the last 6 bytes of the android-id, shifted and scaled them with hardcoded constants to generate the 16-byte aes key. So the key could be brute-forced being just 6 bytes. The `Pixel 9` in the challenge description, thus, had nothing to do with anything.

This seemingly hopelessly inadequate deception tactic surprisingly turned out to be quite effective, We saw a bunch of discord tickets asking how they could solve the challenge without "extra information" (in the form of android_id).

![Image](https://github.com/user-attachments/assets/740905eb-a827-437b-8745-42ab8e6d6ef3)

The plan was, of course, to create notes containing the flag in the app, then create a backup within the app, and then simply distribute the backup along with the apk.

##### Obfuscating the app

Our faithful old proguard, first of all:

```
-keep class com.secure.notes.feature_note.domain.model.** { *; }
-keepattributes Signature
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
```

That isn't adequate, as the resulting apk is still very trivial to reverse engineer.

I decided to simply pass it through a bunch of [Obfuscapk](https://github.com/ClaudiuGeorgiu/Obfuscapk) obfuscators. After a bit of fiddling about I found the longest chain of obfuscators to use while being careful not to break the application.

```
-o Reorder -o MethodRename -o MethodOverload -o ArithmeticBranch -o RandomManifest -o DebugRemoval -o Rebuild -o NewAlignment -o NewSignature
```

I had high hopes that this would churn out something adequately difficult to reverse engineer, but again, the output was was disappointingly trivial to revesrse. That led to the "rip cheap obfuscation" part of the flag `KashiCTF{r1P_Ch34P_o8FuSC471oN_7_7_Nu123_117_8Hu_87W}`.