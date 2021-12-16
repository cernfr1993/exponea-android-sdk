
# Version update

This guide will help you upgrade your Exponea SDK to the new version.

## Updating from version 2.x.x to 3.x.x
 Changes you will need to do when updating Exponea SDK to version 3 and higher are related to push notifications.


### Changes regarding FirebaseMessagingService

 We decided not to include the implementation of FirebaseMessagingService in our SDK since we want to keep it as small as possible and avoid including the libraries that are not essential for its functionality. SDK no longer has a dependency on the firebase library. Changes you will need to do are as follows:

 #### If you are already using your own service that extends FirebaseMessagingService

1. You will have to change the second parameter when calling `Exponea.handleRemoteMessage` method. It no longer accepts firebase `RemoteMessage` but accepts the message data directly.

2. Instead of calling `Exponea.trackPushToken` when a new token is obtained, call `Exponea.handleNewToken` with the application context. This method will not only track new token to the Exponea but if SDK is not initialized at the moment, persist the token and track it after SDK initialization. 

#### If you do not have your own service and you were relying on our implementation
You will have to implement FirebaseMessagingService on your application side since SDK no longer contains it.
1. Create a service that extends FirebaseMessagingService
2. Call `Exponea.handleRemoteMessage` when a message is received
3. Call `Exponea.handleNewToken` when a token is obtained
4. Register this service in your `AndroidManifest.xml`

```kotlin 
class MyFirebaseMessagingService : FirebaseMessagingService() {  
  
    private val notificationManager by lazy {  
  getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager  
    }  
  
  override fun onMessageReceived(message: RemoteMessage) {  
        super.onMessageReceived(message)  
        Exponea.handleRemoteMessage(applicationContext, message.data, notificationManager)  
    }  
  
    override fun onNewToken(token: String) {  
        super.onNewToken(token)  
        Exponea.handleNewToken(applicationContext, token)  
    }  
}
```

``` xml
<service android:name="MyFirebaseMessagingService" android:exported="false">  
	 <intent-filter> 
		 <action android:name="com.google.firebase.MESSAGING_EVENT" />  
	 </intent-filter>
</service>
```

### Changes regarding Notification Trampolining
Android (from version 12) restricted starting any Intent from notification action indirectly. This means an activity can no longer be started from service or receiver. In version 3.0.0 of our SDK, we made changes to comply with these rules. SDK is starting all notification action Intents directly (opening the app, opening a web browser, opening a deep link). In previous versions, you need to create BroadcastReceiver to handle Intent with action `com.exponea.sdk.action.PUSH_CLICKED` and start the app on the application side. This code should be removed now since this receiver acts as a notification trampoline. Unfortunately, when targeting Android S and above, it can cause the app to crash.  You can now safely **remove** this code; opening your Launcher Activity on push click is now handled by the SDK.

``` xml
<receiver
    android:name="MyReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="com.exponea.sdk.action.PUSH_CLICKED" />
    </intent-filter>
</receiver>
```

``` kotlin
class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Extract payload data
        val data = intent.getParcelableExtra<NotificationData>(
          ExponeaPushReceiver.EXTRA_DATA
        )
        // Process the data if you need to

        // Start an activity
        val launchIntent = Intent(context, MainActivity::class.java)
        launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(context, launchIntent, null)
    }
}
```

### Changes in public API

Exponea SDK no longer has a dependency on the Firebase library. For this reason, the signature of public methods that excepted firebase `RemoteMessage` needed to be changed. Now methods directly accept message data instead. You can obtain them by calling `getData()` on `RemoteMessage` object.

```
fun handleRemoteMessage(
  applicationContext: Context,  
  message: RemoteMessage?, 
  manager: NotificationManager,  
  showNotification: Boolean = true  
): Boolean
```
 was changed to
```
fun handleRemoteMessage(  
  applicationContext: Context,  
  messageData: Map<String, String>?,  
  manager: NotificationManager,  
  showNotification: Boolean = true  
): Boolean
```

and 

`fun isExponeaPushNotification(message: RemoteMessage?): Boolean`

was changed to 	

`fun isExponeaPushNotification(messageData: Map<String, String>?): Boolean`