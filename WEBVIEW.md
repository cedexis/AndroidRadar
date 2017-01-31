# WebView Proof of Concept

In order to use the same JS tag there is support to insert a WebView and load a simple HTML loading
the JS tag over all activities.

If you want to use this you have to call (best placed at `onResume` method):

````java 
Radar.init()
````

This will return a `Radar` object that you can use to send events over Activities using:

````java
radar.sendRadarEvent(this, zoneId, customerId);
````

This will load a WebView in your Activity content hidden and will launch everything that you need.

If the customer comes back to this activity, it will lookup for the already deployed `WebView` and
load the script once again to send a Radar event, just as in the web.

This is experimental.