Spydle
======

[![Build Status](https://secure.travis-ci.org/realityforge/spydle.png?branch=master)](http://travis-ci.org/realityforge/spydle)

Spydle collects metrics from your services and feeds it to your metric collection system. Spydle can actively
seek query the monitored systems or passively recive data pushed to it.

Right now Spydle is focused on collecting metrics from JVM based services and pushing the data to Graphite but
expect this to change over time.

TODO
====

Introduce https://github.com/etsy/logster/blob/master/logster as a source of data.

Merge in functionality similar to https://github.com/markchadwick/graphite-relay

Add in Ldap query/probe framework

Add in SOAP query/probe framework

Consider integrating with http://jrds.fr/sourcetype/start
