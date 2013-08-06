videochat
=========

Live streaming Red5 based video chat.

# Introduction

Screenshot:
![video chat screenshot](https://github.com/dzer6/videochat/raw/master/documents/video-chat-screenshot-01.png)

# Structure

Default video chat architecture on diagram below:
![video chat architecture](https://github.com/dzer6/videochat/raw/master/documents/video-chat-architecture.png)
1) Message Broker -- Apache ActiveMQ.

2) Web Front End App -- caelyf based web app, its name "ga3" (actually there is a little bit hacked [caelyf](https://github.com/dzer6/caelyf)).

3) DB -- any relational data base (by default in-memory H2 data base).

4) Red5 App -- red5 based web app for video sending/receiving via RTMP, its name "r5wa".

5) Servlet Container -- Apache Tomcat.

6) Server -- ubuntu based server machine.

7) Flash Video -- openlaszlo based flash part for sending outgoing video from webcam and showing incoming video in window.

8) Browser -- any modern browser.

9) Client -- end user machine.

# Installation [IN PROGRESS]

TODO
