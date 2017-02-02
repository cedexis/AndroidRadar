# Radar events with WebView in Android

In order to use the same JS tag there is support to insert a WebView and load a simple HTML loading
the JS tag over all activities.

If you want to use this you have to call:

```` java 
Radar radarObject = Cedexis.initRadar(zoneId, customerId);
````

This object can be initialized over your `Application#onCreate` method and pass it down to your activities.

`Radar` object can be used to send events over Activities using:

```` java
Radar radarObject = Cedexis.initRadar(zoneId, customerId);
radarObject.sendRadarEvent(YourActivity.this);
````

This will load a WebView in your Activity content hidden and will launch everything that you need.

If your client implements this over the `Activity#onResume` method, it will first create a
`WebView` and then it will lookup for the already deployed `WebView`, load the script once again to 
send a Radar event, just as in the web.

This is experimental.