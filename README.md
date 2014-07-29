Senz SDK
========

使用 Senz SDK，你可以方便地给应用加上情景化信息。

Senz
----

我们使用 `Senz` 对象表示一个情景，它的 `type()` 方法返回一个 `String`，
表示该情景的类别，`subType()` 方法返回一个 `String`，表示该情景的子类别，
`entities()` 方法返回一个 `Map<String, String>`， 表示该情景的附加信息。


监听周围的 Senz
---------------

要监听周围的 `Senz`，使用 `SenzManager` 对象。

首先，实例化一个 `SenzManager` 对象：`SenzManager senzManager =
 new SenzManager(context)`。

然后，把它初始化，

    try {
        senzManager.init();
    }
    catch (Exception e) {
        Log.e(TAG, "unable to initialize senz manager!", e);
    }

接下来就可以开始监听了：

    try {
        senzManager.startTelepathy(new SenzManager.TelepathyCallback() {
            @Override
            public void onDiscover(List<Senz> senzes) {
                runOnUiThread(
                    // some runnable
                );
            }
    
            @Override
            public void onLeave(List<Senz> senzes) {
                runOnUiThread(
                    // some runnable
                );
            }
        });
    }
    catch (Exception e) {
        Log.d(TAG, "unable to start telepathy", e);
    }

Senz SDK 会在“发现”周围的 Senz，或“离开” Senz 时回调。当发现周围有
 Senz 时，SDK 会调用 `onDiscover` 方法，对于每个 Senz，在 30 分钟内
它只会被报告一次。当长时间发现某些 Senz 不在周围时，SDK 会调用
 `onLeave` 方法。回调可能并不发生在 UI 线程中，所以如果要在回调方法
里更新 UI，请使用 `runOnUiThread`。

如果要暂停监听，使用 `senzManager.stopTelepathy()`；如果要彻底停止
监听并释放资源，使用 `senzManager.end()`。

在调用 `startTelepathy` 但没有调用 `stopTelepathy` 时，可以再次调用
 `startTelepathy`，但是只有最后一次提供的回调方法会被调用。


权限和服务
----------

要使用 Senz SDK，你需要在 `AndroidManifest.xml` 的 manifest 节点中
声明以下权限：

    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.INTERNET" />

并在 application 节点中声明以下服务

    <service android:name="com.senz.sdk.service.SenzService" />


获得最近发现的 Senz
-------------------

`senzManager.getLastDiscoveredSenzes()` 方法会返回一个 `List<Senz>`，
里面有最近发现的所有 `Senz`，无论它们是不是在 30 分钟内通过回调方法
报告过。

