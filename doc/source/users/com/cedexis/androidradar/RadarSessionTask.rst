.. java:import:: android.content Context

.. java:import:: android.net ConnectivityManager

.. java:import:: android.net NetworkInfo

.. java:import:: android.os AsyncTask

.. java:import:: android.util Base64

.. java:import:: android.util Log

.. java:import:: android.util Pair

.. java:import:: org.json JSONArray

.. java:import:: org.json JSONException

.. java:import:: org.json JSONObject

.. java:import:: java.io BufferedReader

.. java:import:: java.io ByteArrayOutputStream

.. java:import:: java.io IOException

.. java:import:: java.io InputStream

.. java:import:: java.io InputStreamReader

.. java:import:: java.io OutputStream

.. java:import:: java.io UnsupportedEncodingException

.. java:import:: java.net HttpURLConnection

.. java:import:: java.net MalformedURLException

.. java:import:: java.net ProtocolException

.. java:import:: java.net URL

.. java:import:: java.security SecureRandom

.. java:import:: java.util ArrayList

.. java:import:: java.util Date

.. java:import:: java.util List

.. java:import:: java.util Random

.. java:import:: java.util UUID

.. java:import:: java.util.regex Matcher

.. java:import:: java.util.regex Pattern

RadarSessionTask
================

.. java:package:: com.cedexis.androidradar
   :noindex:

.. java:type:: public class RadarSessionTask extends AsyncTask<RadarSessionProperties, RadarSessionProgress, Void>

Constructors
------------
RadarSessionTask
^^^^^^^^^^^^^^^^

.. java:constructor:: public RadarSessionTask(Context context)
   :outertype: RadarSessionTask

   The constructor for the RadarSessionTask class.

   :param context: The Context in which the task runs. Generally the Activity where the task is created.

Methods
-------
doInBackground
^^^^^^^^^^^^^^

.. java:method:: @Override protected Void doInBackground(RadarSessionProperties... params)
   :outertype: RadarSessionTask

onProgressUpdate
^^^^^^^^^^^^^^^^

.. java:method:: @Override protected void onProgressUpdate(RadarSessionProgress... values)
   :outertype: RadarSessionTask

set_caller
^^^^^^^^^^

.. java:method:: public void set_caller(RadarSessionTaskCaller _caller)
   :outertype: RadarSessionTask

