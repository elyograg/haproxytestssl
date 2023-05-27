# Haproxy SSL Test

A simple multi-threaded HTML page grab tester.

Created as a way to test concurrency in openssl, because openssl 3.x
does a lot of locking that didn't exist in the 1.1 and earlier releases.
This has been shown to have performance woes for very busy websites.
So far this program hasn't been able to illustrate these performance
issues, not sure what might be wrong.
