=============
android-radar
=============

An Android library providing a service to perform Radar measurements in the
background.

Using the Library
=================

The primary way to use the library in an Android app is to include the latest
android-radar.jar file found on the Github `Releases page`_, as follows:

1. Download the latests jar to your project's libs directory.  Refresh the
   project by right-clicking on it in Project/Package Explorer and choosing Refresh.

2. In Eclipse, open the project Properties dialog.  Choose the Java Build Path
   page.  Choose the Libraries tab.  Click the Add JARs... button.  Navigate to
   the the libs directory and choose the android-radar.jar file.

3. Edit your project's AndroidManifest.xml file as follows:

   Include the following uses-permission element inside the manifest element:

   - <uses-permission android:name="android.permission.INTERNET" />
   - <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

   Include the following service element inside the application element:

   - <service android:name="com.cedexis.radar.android.RadarSessionService" />

4. Decide where to initiate a Radar session.  For simplicity's sake, suppose you
   decide to do this in your main activity's onCreate method.

   Here's an example::

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        // Start a Radar session here
        Intent intent = new Intent(this, RadarSessionService.class);
        intent.putExtra("zoneId", 1); // required
        intent.putExtra("customerId", 13363); // required
        intent.putExtra("impact", "some impact value"); // optional
        intent.putExtra("loggingLevel", "debug"); // optional
        this.startService(intent);
    }

   The key things to point out here are the zoneId and customerId values being
   set on the intent.  These are required.  If you're not sure what these are,
   you can get these from the Portal by examining your default
   `JavaScript Radar tag`_.  It references a file with a name like
   xx-yyyyy-radar10.min.js, where xx is your zone id and yyyyy is your
   customer id.

5. Run your app a few times.  If you've enabled logging level down to the debug
   level, you'll probably be able to see evidence of measurements being taken
   in Logcat console.  You should also be able to see data starting to appear
   in your `Portal charts`_.

About Cedexis
=============

Founded in 2009, Cedexis optimizes web performance across data centers, content
delivery networks (CDNs) and clouds, for companies that want to ensure
100% availability and extend their reach to new global markets.  We're
dedicated to building a faster web for everyone in the world.

Please visit us as `www.cedexis.com`_.

.. _`www.cedexis.com`: http://www.cedexis.com/

.. _`Portal charts`: https://portal.cedexis.com/static/charts/index.html#reports/radar/platform-performance

.. _`JavaScript Radar tag`: https://portal.cedexis.com/radar/integration.html

.. _`Releases page`: https://github.com/cedexis/android-radar/releases
