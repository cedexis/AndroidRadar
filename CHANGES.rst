CHANGES
=======

0.0.8
-----

- Use updated java-radar library, which adds a trailing slash to the the
  ProbeServer URL.

0.0.2
-----

- The user can enable logging by passing a "loggingLevel" string with the
  intent to the Radar service.

  Example::

    Intent intent = new Intent(context, RadarSessionService.class);
    intent.putExtra("zoneId", 1);
    intent.putExtra("customerId", 13363);
    intent.putExtra("impact", "some impact value");
    intent.putExtra("loggingLevel", "debug");
    context.startService(intent);

0.0.1
-----

- Initial implementation

- Publish android-radar.jar
