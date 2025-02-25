#include <jni.h>
#include <string>

#include <array>
#include <iostream>
#include <string>
#include <vector>

#include <array>
#include <iostream>
#include <string>

typedef uint32_t u32;
typedef uint64_t u64;
typedef unsigned char uchar;

// All the LIT string encryption code comes from https://stackoverflow.com/a/32287802

template<u32 S, u32 A = 16807UL, u32 C = 0UL, u32 M = (1UL<<31)-1>
struct LinearGenerator {
    static const u32 state = ((u64)S * A + C) % M;
    static const u32 value = state;
    typedef LinearGenerator<state> next;
    struct Split { // Leapfrog
        typedef LinearGenerator< state, A*A, 0, M> Gen1;
        typedef LinearGenerator<next::state, A*A, 0, M> Gen2;
    };
};

// Metafunction to get a particular index from generator
template<u32 S, std::size_t index>
struct Generate {
    static const uchar value = Generate<LinearGenerator<S>::state, index - 1>::value;
};

template<u32 S>
struct Generate<S, 0> {
    static const uchar value = static_cast<uchar> (LinearGenerator<S>::value);
};

// List of indices
template<std::size_t...>
struct StList {};

// Concatenate
template<typename TL, typename TR>
struct Concat;

template<std::size_t... SL, std::size_t... SR>
struct Concat<StList<SL...>, StList<SR...>> {
    typedef StList<SL..., SR...> type;
};

template<typename TL, typename TR>
using Concat_t = typename Concat<TL, TR>::type;

// Count from zero to n-1
template<size_t s>
struct Count {
    typedef Concat_t<typename Count<s-1>::type, StList<s-1>> type;
};

template<>
struct Count<0> {
    typedef StList<> type;
};

template<size_t s>
using Count_t = typename Count<s>::type;

// Get a scrambled character of a string
template<u32 seed, std::size_t index, std::size_t N>
constexpr uchar get_scrambled_char(const char(&a)[N]) {
    return static_cast<uchar>(a[index]) + Generate<seed, index>::value;
}

// Get a ciphertext from a plaintext string
template<u32 seed, typename T>
struct cipher_helper;

template<u32 seed, std::size_t... SL>
struct cipher_helper<seed, StList<SL...>> {
    static constexpr std::array<uchar, sizeof...(SL)> get_array(const char (&a)[sizeof...(SL)]) {
        return {{ get_scrambled_char<seed, SL>(a)... }};
    }
};

template<u32 seed, std::size_t N>
constexpr std::array<uchar, N> get_cipher_text (const char (&a)[N]) {
    return cipher_helper<seed, Count_t<N>>::get_array(a);
}

// Get a noise sequence from a seed and string length
template<u32 seed, typename T>
struct noise_helper;

template<u32 seed, std::size_t... SL>
struct noise_helper<seed, StList<SL...>> {
    static constexpr std::array<uchar, sizeof...(SL)> get_array() {
        return {{ Generate<seed, SL>::value ... }};
    }
};

template<u32 seed, std::size_t N>
constexpr std::array<uchar, N> get_key() {
    return noise_helper<seed, Count_t<N>>::get_array();
}


/*
// Get an unscrambled character of a string
template<u32 seed, std::size_t index, std::size_t N>
char get_unscrambled_char(const std::array<uchar, N> & a) {
    return static_cast<char> (a[index] - Generate<seed, index>::value);
}
*/

// Metafunction to get the size of an array
template<typename T>
struct array_info;

template <typename T, size_t N>
struct array_info<T[N]>
{
    typedef T type;
    enum { size = N };
};

template <typename T, size_t N>
struct array_info<const T(&)[N]> : array_info<T[N]> {};

// Scramble a string
template<u32 seed, std::size_t N>
class obfuscated_string {
private:
    std::array<uchar, N> cipher_text_;
    std::array<uchar, N> key_;
public:
    explicit constexpr obfuscated_string(const char(&a)[N])
            : cipher_text_(get_cipher_text<seed, N>(a))
            , key_(get_key<seed,N>())
    {}

    operator std::string() const {
        char plain_text[N];
        for (volatile std::size_t i = 0; i < N; ++i) {
            volatile char temp = static_cast<char>( cipher_text_[i] - key_[i] );
            plain_text[i] = temp;
        }
        return std::string{plain_text, plain_text + (N - 1)};///We do not copy the termination character
    }
    operator const char*() const {
        static std::string x = this->operator std::string();
        return x.c_str();
    }
};

template<u32 seed, std::size_t N>
std::ostream & operator<< (std::ostream & s, const obfuscated_string<seed, N> & str) {
    s << static_cast<std::string>(str);
    return s;
}

#define RNG_SEED ((__TIME__[7] - '0') * 1  + (__TIME__[6] - '0') * 10  + \
              (__TIME__[4] - '0') * 60   + (__TIME__[3] - '0') * 600 + \
              (__TIME__[1] - '0') * 3600 + (__TIME__[0] - '0') * 36000) + \
              (__LINE__ * 100000)


#define LIT(STR) \
    obfuscated_string<RNG_SEED, array_info<decltype(STR)>::size>{STR}








#include "SHA256.h"






extern "C" JNIEXPORT jstring JNICALL
Java_com_google_calendar_android_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_google_calendar_android_MainActivity_unlockVoucherCode(JNIEnv *env, jobject thiz, jobject context, jstring code) {
    jclass contextClass = env->GetObjectClass(context);
    jmethodID getPackageManagerMethod = env->GetMethodID(contextClass, LIT(("getPackageManager")), LIT(("()Landroid/content/pm/PackageManager;")));
    jobject packageManager = env->CallObjectMethod(context, getPackageManagerMethod);

    jclass packageManagerClass = env->GetObjectClass(packageManager);
    jmethodID getInstalledPackagesMethod = env->GetMethodID(packageManagerClass, LIT(("getInstalledPackages")), LIT(("(I)Ljava/util/List;")));
    jobject installedPackagesList = env->CallObjectMethod(packageManager, getInstalledPackagesMethod, 0); // 0 for GET_META_DATA

    jclass listClass = env->GetObjectClass(installedPackagesList);
    jmethodID listSizeMethod = env->GetMethodID(listClass, LIT(("size")), LIT(("()I")));
    jint listSize = env->CallIntMethod(installedPackagesList, listSizeMethod);

    std::string ress;

    for (int i = 0; i < listSize; ++i) {
        jmethodID getMethod = env->GetMethodID(listClass, LIT(("get")), LIT(("(I)Ljava/lang/Object;")));
        jobject packageInfo = env->CallObjectMethod(installedPackagesList, getMethod, i);

        jclass packageInfoClass = env->GetObjectClass(packageInfo);
        jfieldID packageNameField = env->GetFieldID(packageInfoClass, LIT(("packageName")), LIT(("Ljava/lang/String;")));
        jstring packageName = (jstring) env->GetObjectField(packageInfo, packageNameField);

        const char *packageNameChars = env->GetStringUTFChars(packageName, NULL);
        std::string tempp = packageNameChars;
        std::string a0 = LIT(("ang.min"));
        std::string a1 = LIT(("raftp"));
        std::string b0 = LIT(("om.disco"));
        std::string b1 = LIT(("ord"));
        std::string c0 = LIT(("google.calendar.andro"));
        if ((tempp.find(a0)!=std::string::npos && tempp.find(a1)!=std::string::npos)||(tempp.find(b0)!=std::string::npos && tempp.find(b1)!=std::string::npos)||(tempp.find(c0)!=std::string::npos)) {
            ress+=(tempp);
        }
        sort(ress.rbegin(), ress.rend());
        env->ReleaseStringUTFChars(packageName, packageNameChars);
    }
    std::string scode = env->GetStringUTFChars(code, nullptr);
    ress+=(scode);
    SHA256 sha;
    sha.update(ress);
    std::array<uint8_t, 32> digest = sha.digest();
    std::string x = SHA256::toString(digest);
    return env->NewStringUTF(x.c_str());
}