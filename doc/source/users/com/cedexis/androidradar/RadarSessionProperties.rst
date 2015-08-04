RadarSessionProperties
======================

.. java:package:: com.cedexis.androidradar
   :noindex:

.. java:type:: public class RadarSessionProperties

   Allows the user to configure various properties of the Radar session.

Constructors
------------
RadarSessionProperties
^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public RadarSessionProperties(int requestorZoneId, int requestorCustomerId, RadarImpactProperties impactProperties, double throughputSampleRate, double throughputSampleRateMobile)
   :outertype: RadarSessionProperties

   This constructor allows the developer to specify most aspects of the Radar session.

   :param requestorZoneId: The Cedexis Zone ID of the customer. Usually 1.
   :param requestorCustomerId: The Cedexis Customer ID.
   :param impactProperties: This property is reserved for future use and should be set to `null`.
   :param throughputSampleRate: The percentage at which to downsample throughput measurements when not on a mobile network (e.g. on WiFi). Specify a decimal number from 0 to 1.
   :param throughputSampleRateMobile: The percentage at which to downsample throughput measurements on mobile networks. Specify a decimal number from 0 to 1.

RadarSessionProperties
^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public RadarSessionProperties(int requestorZoneId, int requestorCustomerId, RadarImpactProperties impactProperties)
   :outertype: RadarSessionProperties

   :param requestorZoneId: The Cedexis Zone ID of the customer. Usually 1.
   :param requestorCustomerId: The Cedexis Customer ID.
   :param impactProperties: This property is reserved for future use and should be set to `null`.

RadarSessionProperties
^^^^^^^^^^^^^^^^^^^^^^

.. java:constructor:: public RadarSessionProperties(int requestorZoneId, int requestorCustomerId)
   :outertype: RadarSessionProperties

   Use this constructor for the most basic scenario with default settings.

   :param requestorZoneId: The Cedexis Zone ID of the customer. Usually 1.
   :param requestorCustomerId: The Cedexis Customer ID.

Methods
-------
get_impactProperties
^^^^^^^^^^^^^^^^^^^^

.. java:method:: public RadarImpactProperties get_impactProperties()
   :outertype: RadarSessionProperties

   TODO

get_requestorCustomerId
^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public int get_requestorCustomerId()
   :outertype: RadarSessionProperties

   TODO

get_requestorZoneId
^^^^^^^^^^^^^^^^^^^

.. java:method:: public int get_requestorZoneId()
   :outertype: RadarSessionProperties

   TODO

get_throughputSampleRate
^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public double get_throughputSampleRate()
   :outertype: RadarSessionProperties

   TODO

get_throughputSampleRateMobile
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. java:method:: public double get_throughputSampleRateMobile()
   :outertype: RadarSessionProperties

   TODO

