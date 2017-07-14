[![npm][npm-badge]][npm]
[![react-native][rn-badge]][rn]
[![MIT][license-badge]][license]
[![bitHound Score][bithound-badge]][bithound]
[![Downloads](https://img.shields.io/npm/dm/rnkit-in-app-update.svg)](https://www.npmjs.com/package/rnkit-in-app-update)

InApp Update for react-native for [React Native][rn].

[**Support me with a Follow**](https://github.com/simman/followers)

[npm-badge]: https://img.shields.io/npm/v/rnkit-in-app-update.svg
[npm]: https://www.npmjs.com/package/rnkit-in-app-update
[rn-badge]: https://img.shields.io/badge/react--native-v0.40-05A5D1.svg
[rn]: https://facebook.github.io/react-native
[license-badge]: https://img.shields.io/dub/l/vibe-d.svg
[license]: https://raw.githubusercontent.com/rnkit/rnkit-in-app-update/master/LICENSE
[bithound-badge]: https://www.bithound.io/github/rnkit/rnkit-in-app-update/badges/score.svg
[bithound]: https://www.bithound.io/github/rnkit/rnkit-in-app-update

## Getting Started

First, `cd` to your RN project directory, and install RNMK through [rnpm](https://github.com/rnpm/rnpm) . If you don't have rnpm, you can install RNMK from npm with the command `npm i -S rnkit-in-app-update` and link it manually (see below).

### Android

* #### React Native < 0.29 (Using rnpm)

  `rnpm install rnkit-in-app-update`

* #### React Native >= 0.29
  `$npm install -S rnkit-in-app-update`

  `$react-native link rnkit-in-app-update`

#### Manually
1. JDK 7+ is required
1. Add the following snippet to your `android/settings.gradle`:

  ```gradle
include ':rnkit-in-app-update'
project(':rnkit-in-app-update').projectDir = new File(rootProject.projectDir, '../node_modules/rnkit-in-app-update/android/app')
  ```
  
1. Declare the dependency in your `android/app/build.gradle`
  
  ```gradle
  dependencies {
      ...
      compile project(':rnkit-in-app-update')
  }
  ```
  
1. Import `import io.rnkit.inappupdate.InAppUpdatePackage;` and register it in your `MainActivity` (or equivalent, RN >= 0.32 MainApplication.java):

  ```java
  @Override
  protected List<ReactPackage> getPackages() {
      return Arrays.asList(
              new MainReactPackage(),
              new InAppUpdatePackage()
      );
  }
  ```

Finally, you're good to go, feel free to require `rnkit-in-app-update` in your JS files.

Have fun! :metal:

## Basic Usage

Import library

```
import InAppUpdate from 'rnkit-in-app-update'
```


## Contribution

- [@ws123](https://github.com/ws123) The main author.
- [@simamn](mailto:liwei0990@gmail.com)

## Thanks

[@yjfnypeu](https://github.com/yjfnypeu) - [UpdatePlugin](https://github.com/yjfnypeu/UpdatePlugin)

# Api

```
/**
 * 获取安卓的metadata信息, 主要用于获取渠道号
 * @param {String} [required] key 对应的key
 */
async getAppMetaData(key);

/**
 * 下载Apk文件
 * @param {Object} options 下载参数
 * ------- options ---------
 * @param {String} [required] url apk 下载地址
 * @param {String} [required] version 版本号
 * @param {String} algorithm 可选(md5, etag), 当文件下载完成后会返回对应的值
 * @param {Bool} isForce 为true的情况下，会忽略网络条件和下载策略
 * @param {Bool} strategy 0默认，表示只判断当前环境，如果是wifi就下载，否则就不下载, 1表示监听wifi，如果当前是wifi就下载，如果当前不是wifi，就监听网络变化，如果在程序运行期间，切换到了wifi，就提示用户下载
 */
downloadApk(options);

/**
 * 安卓应用
 */
installApk();

/**
 * 暂停下载
 * 调用此方法, 系统会发送事件RNKitApkDownloadPause, type: manual
 */
pauseDownload();

/**
 * 继续下载
 */
downloadResume();

--------------- 新增事件回调 ------------
开始下载： RNKitApkStartDownload
下载进度： RNKitApkDownloadProgress, return params{received, total}
下载错误: RNKitApkDownloadError, return params{errorMsg}
下载被暂停了: RNKitApkDownloadPause, return params{type}, type: (manual, network)
下载完成了: RNKitApkDownloadComplete, return params{md5, etag, path, success}

```


## Questions

Feel free to [contact me](mailto:liwei0990@gmail.com) or [create an issue](https://github.com/rnkit/rnkit-in-app-update/issues/new)

> made with ♥