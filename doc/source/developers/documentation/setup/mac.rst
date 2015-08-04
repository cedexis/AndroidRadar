Mac
===

The documentation is built using Sphinx and javasphinx.  These can be installed
to your local machine as follows.

Prerequesites
-------------

Install lxml using Homebrew::

    brew install libxml2
    brew install libxslt
    brew link libxml2 --force
    brew link libxslt --force

Install Python packages:

.. important::

   Be sure to use Python 2.7.* and an installation of pip that will install
   package for it.  Depending on your system, you may need to execute `pip2` or
   `pip2.7` if the default `pip` command would update Python 3 site packages.

.. code-block:: bash

    $ pip2 install Sphinx javasphinx --user
